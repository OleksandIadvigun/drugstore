package sigma.software.leovegas.drugstore.order

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.matching.ContainsPattern
import com.github.tomakehurst.wiremock.matching.EqualToPattern
import com.google.protobuf.ByteString
import java.math.BigDecimal
import java.time.LocalDateTime
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.accountancy.api.ConfirmOrderResponse
import sigma.software.leovegas.drugstore.accountancy.api.CreateOutcomeInvoiceEvent
import sigma.software.leovegas.drugstore.accountancy.api.ItemDTO
import sigma.software.leovegas.drugstore.api.protobuf.Proto
import sigma.software.leovegas.drugstore.api.toDecimalProto
import sigma.software.leovegas.drugstore.infrastructure.extensions.get
import sigma.software.leovegas.drugstore.infrastructure.extensions.withProtobufResponse
import sigma.software.leovegas.drugstore.order.api.CreateOrderEvent
import sigma.software.leovegas.drugstore.order.api.OrderItemDTO
import sigma.software.leovegas.drugstore.order.api.OrderStatusDTO
import sigma.software.leovegas.drugstore.order.api.UpdateOrderEvent

@AutoConfigureTestDatabase
@DisplayName("OrderService test")
@Import(CustomTestConfig::class)
class OrderServiceTest @Autowired constructor(
    val transactionTemplate: TransactionTemplate,
    val orderItemRepository: OrderItemRepository,
    val orderRepository: OrderRepository,
    val orderService: OrderService,
    val objectMapper: ObjectMapper,
) : WireMockTest() {

    @Test
    fun `should create order`() {

        // setup
        transactionTemplate.execute { orderItemRepository.deleteAllInBatch() }
        transactionTemplate.execute { orderRepository.deleteAllInBatch() }

        // given
        val order = CreateOrderEvent(
            orderNumber = "1",
            orderItems = listOf(
                OrderItemDTO(
                    productNumber = "1",
                    quantity = 3
                )
            )
        )

        // when
        val created = transactionTemplate.execute {
            orderService.createOrder(order)
        }.get()

        // then
        assertThat(created.orderNumber).isNotEqualTo("undefined")
        assertThat(created.orderStatus).isEqualTo(OrderStatusDTO.CREATED)
        assertThat(created.orderItems[0].quantity).isEqualTo(3)
        assertThat(created.orderItems[0].productNumber).isEqualTo("1")
        assertThat(created.createdAt).isBeforeOrEqualTo(LocalDateTime.now())
        assertThat(created.updatedAt).isAfterOrEqualTo(created.createdAt)
    }

    @Test
    fun `should not create order without orderItems`() {

        // setup
        transactionTemplate.execute { orderItemRepository.deleteAllInBatch() }
        transactionTemplate.execute { orderRepository.deleteAllInBatch() }

        // when
        val exception = assertThrows<InsufficientAmountOfOrderItemException> {
            orderService.createOrder(CreateOrderEvent(orderItems = listOf()))
        }

        // then
        assertThat(exception.message).contains("You have to add minimum one order item")
    }

    @Test
    fun `should get order by order number `() {

        // setup
        transactionTemplate.execute { orderItemRepository.deleteAllInBatch() }
        transactionTemplate.execute { orderRepository.deleteAllInBatch() }

        // given
        val created = transactionTemplate.execute {
            orderRepository.save(
                Order(
                    orderNumber = "1",
                    orderItems = setOf(
                        OrderItem(
                            productNumber = "1",
                            quantity = 3
                        )
                    )
                )
            )
        }?.toOrderResponseDTO().get()

        // when
        val actual = orderService.getOrderByOrderNumber(created.orderNumber)

        // then
        assertThat(actual.orderNumber).isEqualTo(created.orderNumber)
        assertThat(actual.orderItems.iterator().next().productNumber).isEqualTo("1")
        assertThat(actual.orderItems.iterator().next().quantity).isEqualTo(3)
    }

    @Test
    fun `should not get non existing order`() {

        // setup
        transactionTemplate.execute { orderItemRepository.deleteAllInBatch() }
        transactionTemplate.execute { orderRepository.deleteAllInBatch() }

        // given
        val nonExistingNumber = "not"

        // when
        val exception = assertThrows<OrderNotFoundException> { orderService.getOrderByOrderNumber(nonExistingNumber) }

        // then
        assertThat(exception.message).contains("Order", "was not found")
    }

    @Test
    fun `should get order by status `() {

        // given
        transactionTemplate.execute { orderItemRepository.deleteAllInBatch() }
        transactionTemplate.execute { orderRepository.deleteAllInBatch() }

        // and
        val created = transactionTemplate.execute {
            orderRepository.save(
                Order(
                    orderNumber = "1",
                    orderItems = setOf(
                        OrderItem(
                            productNumber = "1",
                            quantity = 3
                        )
                    ),
                    orderStatus = OrderStatus.CREATED
                )
            )
        }?.toOrderResponseDTO().get()

        // when
        val actual = orderService.getOrdersByStatus(OrderStatusDTO.CREATED)

        // then
        assertThat(actual[0].orderNumber).isEqualTo(created.orderNumber)
        assertThat(actual[0].orderItems.iterator().next().productNumber).isEqualTo("1")
        assertThat(actual[0].orderItems.iterator().next().quantity).isEqualTo(3)
        assertThat(actual[0].orderStatus).isEqualTo(OrderStatusDTO.CREATED)
    }

    @Test
    fun `should get all orders`() {

        // given
        transactionTemplate.execute { orderItemRepository.deleteAllInBatch() }
        transactionTemplate.execute { orderRepository.deleteAllInBatch() }

        // and
        transactionTemplate.execute {
            orderRepository.saveAll(
                listOf(
                    Order(
                        orderStatus = OrderStatus.CONFIRMED,
                        orderNumber = "1",
                        orderItems = setOf(
                            OrderItem(
                                productNumber = "1",
                                quantity = 2
                            ),
                        )
                    ),
                    Order(
                        orderStatus = OrderStatus.CONFIRMED,
                        orderNumber = "2",
                        orderItems = setOf(
                            OrderItem(
                                productNumber = "3",
                                quantity = 4
                            ),
                        )
                    )
                )
            )
        }

        // when
        val orders = orderService.getOrders(page = 0, size = 5)

        // then
        assertThat(orders).hasSize(2)
    }

    @Test
    fun `should update order`() {

        // setup
        transactionTemplate.execute { orderItemRepository.deleteAllInBatch() }
        transactionTemplate.execute { orderRepository.deleteAllInBatch() }

        // given
        val orderToChange = transactionTemplate.execute {
            orderRepository.save(
                Order(
                    orderNumber = "1",
                    orderItems = setOf(
                        OrderItem(
                            productNumber = "1",
                            quantity = 3
                        )
                    ),
                )
            )
        }?.toOrderResponseDTO().get()

        // and
        val updateOrderEvent = UpdateOrderEvent(
            orderNumber = orderToChange.orderNumber,
            orderItems = listOf(
                OrderItemDTO(
                    productNumber = "1",
                    quantity = 4
                )
            )
        )

        // when
        val changedOrder = transactionTemplate.execute {
            orderService.updateOrder(updateOrderEvent)
        }.get()

        // then
        assertThat(changedOrder.orderNumber).isEqualTo("1")
        assertThat(changedOrder.orderItems.iterator().next().quantity).isEqualTo(4)
        assertThat(changedOrder.orderItems.iterator().next().productNumber).isEqualTo("1")
        assertThat(changedOrder.orderStatus).isEqualTo(OrderStatusDTO.UPDATED)
        assertThat(changedOrder.createdAt).isBeforeOrEqualTo(LocalDateTime.now())
        assertThat(changedOrder.updatedAt).isAfterOrEqualTo(changedOrder.createdAt)
    }

    @Test
    fun `should change order status`() {

        // setup
        transactionTemplate.execute { orderItemRepository.deleteAllInBatch() }
        transactionTemplate.execute { orderRepository.deleteAllInBatch() }

        // given
        val orderToChange = transactionTemplate.execute {
            orderRepository.save(
                Order(
                    orderNumber = "1",
                    orderItems = setOf(
                        OrderItem(
                            productNumber = "1",
                            quantity = 3
                        )
                    ),
                    orderStatus = OrderStatus.CREATED
                )
            )
        }?.toOrderResponseDTO().get()

        // when
        val changedOrder = transactionTemplate.execute {
            orderService.changeOrderStatus(orderToChange.orderNumber, OrderStatusDTO.CONFIRMED)
        }.get()

        // then
        assertThat(changedOrder.orderNumber).isEqualTo(orderToChange.orderNumber)
        assertThat(changedOrder.orderStatus).isEqualTo(OrderStatusDTO.CONFIRMED)
    }

    @Test
    fun `should not update order without orderItems`() {

        // setup
        transactionTemplate.execute { orderItemRepository.deleteAllInBatch() }
        transactionTemplate.execute { orderRepository.deleteAllInBatch() }

        // given
        val orderToUpdate = transactionTemplate.execute {
            orderRepository.save(
                Order(
                    orderNumber = "1",
                    orderItems = setOf(
                        OrderItem(
                            productNumber = "1",
                            quantity = 3
                        )
                    ),
                )
            )
        }?.toOrderResponseDTO().get()

        // when
        val exception = assertThrows<InsufficientAmountOfOrderItemException> {
            orderService.updateOrder(UpdateOrderEvent(orderToUpdate.orderNumber, listOf()))
        }

        // then
        assertThat(exception.message).contains("You have to add minimum one order item")
    }

    @Test
    fun `should not update non existing order`() {

        // setup
        transactionTemplate.execute { orderItemRepository.deleteAllInBatch() }
        transactionTemplate.execute { orderRepository.deleteAllInBatch() }

        // given
        val nonExitingOrderNumber = "not"

        // and
        val request = UpdateOrderEvent(
            nonExitingOrderNumber,
            listOf(
                OrderItemDTO(
                    productNumber = "5",
                    quantity = 2
                )
            )
        )

        // when
        val exception = assertThrows<OrderNotFoundException> {
            orderService.updateOrder(request)
        }

        // then
        assertThat(exception.message).contains("Order", "was not found")
    }

    @Test
    fun `should get order details`() {

        // setup
        transactionTemplate.execute { orderItemRepository.deleteAllInBatch() }
        transactionTemplate.execute { orderRepository.deleteAllInBatch() }

        // given
        val order = transactionTemplate.execute {
            orderRepository.save(
                Order(
                    orderNumber = "1",
                    orderStatus = OrderStatus.CREATED,
                    orderItems = setOf(
                        OrderItem(
                            productNumber = "1",
                            quantity = 1
                        ),
                        OrderItem(
                            productNumber = "2",
                            quantity = 2
                        )
                    )
                )
            )
        }.get()

        // and
        val productsProto = listOf(
            Proto.ProductDetailsItem.newBuilder()
                .setName("test1").setProductNumber("1").setQuantity(3)
                .setPrice(BigDecimal("20.00").toDecimalProto())
                .build(),
            Proto.ProductDetailsItem.newBuilder()
                .setName("test2").setProductNumber("2").setQuantity(4)
                .setPrice(BigDecimal("30.00").toDecimalProto())
                .build()
        )
        Proto.ProductDetailsResponse.newBuilder().addAllProducts(productsProto).build()

        // given
        stubFor(
            WireMock.get("/api/v1/products/details?productNumbers=1&productNumbers=2")
                .willReturn(
                    aResponse()
                        .withProtobufResponse {
                            Proto.ProductDetailsResponse.newBuilder().addAllProducts(productsProto).build()
                        }
                )
        )

        // and
        val price = BigDecimal("40.00")
        val price2 = BigDecimal("40.00")
        val protoPrice = Proto.DecimalValue.newBuilder()
            .setPrecision(price.precision())
            .setScale(price.scale())
            .setValue(ByteString.copyFrom(price.unscaledValue().toByteArray()))
            .build()
        val protoPrice2 = Proto.DecimalValue.newBuilder()
            .setPrecision(price2.precision())
            .setScale(price2.scale())
            .setValue(ByteString.copyFrom(price2.unscaledValue().toByteArray()))
            .build()
        val responseEProto = Proto.ProductsPrice.newBuilder()
            .putItems("1", protoPrice)
            .putItems("2", protoPrice2)
            .build()

        // and
        stubFor(
            WireMock.get("/api/v1/accountancy/sale-price?productNumbers=1&productNumbers=2")
                .willReturn(
                    aResponse()
                        .withProtobufResponse { responseEProto }
                        .withStatus(HttpStatus.OK.value())
                )
        )

        // when
        val orderDetails = orderService.getOrderDetails(order.orderNumber)

        // then
        assertThat(orderDetails.orderItemDetails).hasSize(2)
        assertThat(orderDetails.orderNumber).isEqualTo("1")
        assertThat(orderDetails.orderItemDetails.iterator().next().productNumber).isEqualTo("1")
        assertThat(orderDetails.orderItemDetails.iterator().next().name).isEqualTo("test1")
        assertThat(orderDetails.orderItemDetails.iterator().next().quantity).isEqualTo(1)
        assertThat(orderDetails.orderItemDetails.iterator().next().price).isEqualTo(BigDecimal("40.00").setScale(2))
        assertThat(orderDetails.total).isEqualTo(BigDecimal("120").setScale(2)) // price multiply quantity
    }

    @Test
    fun `should not get order details for order with status None`() {

        // setup
        transactionTemplate.execute { orderItemRepository.deleteAllInBatch() }
        transactionTemplate.execute { orderRepository.deleteAllInBatch() }

        // given
        val order = transactionTemplate.execute {
            orderRepository.save(
                Order(
                    orderNumber = "1",
                    orderStatus = OrderStatus.NONE,
                    orderItems = setOf(
                        OrderItem(
                            productNumber = "1",
                            quantity = 1
                        ),
                    )
                )
            )
        }.get()

        // when
        val exception = assertThrows<OrderNotCreatedException> {
            orderService.getOrderDetails(order.orderNumber)
        }

        // then
        assertThat(exception.message).contains("Order", " must be created")
    }

    @Test
    fun `should confirm order`() {

        // setup
        transactionTemplate.execute { orderItemRepository.deleteAllInBatch() }
        transactionTemplate.execute { orderRepository.deleteAllInBatch() }

        // given
        val order = transactionTemplate.execute {
            orderRepository.save(
                Order(
                    orderNumber = "1",
                    orderStatus = OrderStatus.CREATED,
                    orderItems = setOf(
                        OrderItem(
                            productNumber = "1",
                            quantity = 1
                        ),
                    )
                )
            )
        }.get()

        // and
        stubFor(
            post("/api/v1/accountancy/invoice/outcome")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
                .withRequestBody(
                    EqualToPattern(
                        objectMapper
                            .writerWithDefaultPrettyPrinter()
                            .writeValueAsString(
                                CreateOutcomeInvoiceEvent(
                                    listOf(
                                        ItemDTO(
                                            productNumber = "1",
                                            quantity = 1
                                        )
                                    ), order.orderNumber
                                )
                            )
                    )
                )
                .willReturn(
                    aResponse()
                        .withBody(
                            objectMapper
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(
                                    ConfirmOrderResponse(
                                        orderNumber = order.orderNumber,
                                        amount = BigDecimal("20.00"),
                                    )
                                )
                        )
                )
        )

        // when
        val response = orderService.confirmOrder(order.orderNumber)

        // then
        assertThat(response).isEqualTo("Confirmed")
    }

    @Test
    fun `should not confirm empty order `() {

        // given
        val order = transactionTemplate.execute {
            orderRepository.save(
                Order(
                    orderNumber = "1",
                    orderStatus = OrderStatus.CONFIRMED,
                )
            )
        }.get()

        // when
        val exception = assertThrows<InsufficientAmountOfOrderItemException> {
            orderService.confirmOrder(order.orderNumber)
        }

        // then
        assertThat(exception.message).contains("You have to add minimum one order item")
    }

    @Test
    fun `should get productId to quantity sorted by quantity`() {

        // given
        transactionTemplate.execute { orderItemRepository.deleteAllInBatch() }
        transactionTemplate.execute { orderRepository.deleteAllInBatch() }

        // and
        transactionTemplate.execute {
            orderRepository.saveAll(
                listOf(
                    Order(
                        orderNumber = "1",
                        orderStatus = OrderStatus.CONFIRMED,
                        orderItems = setOf(
                            OrderItem(
                                productNumber = "3",
                                quantity = 2
                            ),
                        )
                    ),
                    Order(
                        orderNumber = "2",
                        orderStatus = OrderStatus.CONFIRMED,
                        orderItems = setOf(
                            OrderItem(
                                productNumber = "5",
                                quantity = 7
                            ),
                        )
                    ),
                    Order(
                        orderNumber = "3",
                        orderStatus = OrderStatus.CONFIRMED,
                        orderItems = setOf(
                            OrderItem(
                                productNumber = "1",
                                quantity = 4
                            ),
                            OrderItem(
                                productNumber = "5",
                                quantity = 2
                            )
                        )
                    ),
                    Order(
                        orderNumber = "4",
                        orderStatus = OrderStatus.CREATED,
                        orderItems = setOf(
                            OrderItem(
                                productNumber = "2",
                                quantity = 5
                            )
                        )
                    )
                )
            )
        }.get()

        // when
        val sortedItems = orderService.getProductsNumberToQuantity()
        // then
        assertThat(sortedItems.productQuantityItemMap).hasSize(3)
        assertThat(sortedItems.productQuantityItemMap.iterator().next().value).isEqualTo(9)
        assertThat(sortedItems.productQuantityItemMap["1"]).isEqualTo(4)
        assertThat(sortedItems.productQuantityItemMap["3"]).isEqualTo(2)
        assertThat(sortedItems.productQuantityItemMap["5"]).isEqualTo(9)
    }
}
