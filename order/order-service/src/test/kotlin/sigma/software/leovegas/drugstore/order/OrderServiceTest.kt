package sigma.software.leovegas.drugstore.order

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.matching.ContainsPattern
import com.github.tomakehurst.wiremock.matching.EqualToPattern
import java.math.BigDecimal
import java.time.LocalDateTime
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.http.MediaType
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.accountancy.api.InvoiceResponse
import sigma.software.leovegas.drugstore.accountancy.api.InvoiceStatusDTO
import sigma.software.leovegas.drugstore.order.api.CreateOrderRequest
import sigma.software.leovegas.drugstore.order.api.OrderItemDTO
import sigma.software.leovegas.drugstore.order.api.OrderStatusDTO
import sigma.software.leovegas.drugstore.order.api.UpdateOrderRequest
import sigma.software.leovegas.drugstore.product.api.ProductDetailsResponse

@AutoConfigureTestDatabase
@DisplayName("OrderService test")
@AutoConfigureWireMock(port = 8081)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderServiceTest @Autowired constructor(
    val transactionTemplate: TransactionTemplate,
    val orderService: OrderService,
    val orderRepository: OrderRepository,
    val objectMapper: ObjectMapper,
) {

    @Test
    fun `should create order`() {

        // given
        val order = CreateOrderRequest(
            listOf(
                OrderItemDTO(
                    productId = 1,
                    quantity = 3
                )
            )
        )

        // when
        val created = transactionTemplate.execute {
            orderService.createOrder(order)
        } ?: fail("result is expected")

        // then
        assertThat(created.id).isNotNull
        assertThat(created.orderStatus).isEqualTo(OrderStatusDTO.CREATED)
        assertThat(created.orderItems[0].quantity).isEqualTo(3)
        assertThat(created.orderItems[0].productId).isEqualTo(1)
        assertThat(created.createdAt).isBeforeOrEqualTo(LocalDateTime.now())
        assertThat(created.updatedAt).isAfterOrEqualTo(created.createdAt)
    }

    @Test
    fun `should not create order without orderItems`() {

        // when
        val exception = assertThrows<InsufficientAmountOfOrderItemException> {
            orderService.createOrder(CreateOrderRequest(listOf()))
        }

        // then
        assertThat(exception.message).contains("You have to add minimum one order item")
    }

    @Test
    fun `should get order by id `() {

        // given
        val created = transactionTemplate.execute {
            orderRepository.save(
                Order(
                    orderItems = setOf(
                        OrderItem(
                            productId = 1,
                            quantity = 3
                        )
                    )
                )
            )
        }?.toOrderResponseDTO() ?: fail("result is expected")

        // when
        val actual = orderService.getOrderById(created.id)

        // then
        assertThat(actual.id).isEqualTo(created.id)
        assertThat(actual.orderItems.iterator().next().productId).isEqualTo(1)
        assertThat(actual.orderItems.iterator().next().quantity).isEqualTo(3)
    }

    @Test
    fun `should not get non existing order`() {

        // given
        val nonExistingId = -15L

        // when
        val exception = assertThrows<OrderNotFoundException> { orderService.getOrderById(nonExistingId) }

        // then
        assertThat(exception.message).contains("Order", "was not found")
    }

    @Test
    fun `should get order by status `() {

        // given
        transactionTemplate.execute {
            orderRepository.deleteAll()
        }

        // and
        val created = transactionTemplate.execute {
            orderRepository.save(
                Order(
                    orderItems = setOf(
                        OrderItem(
                            productId = 1,
                            quantity = 3
                        )
                    ),
                    orderStatus = OrderStatus.CREATED
                )
            )
        }?.toOrderResponseDTO() ?: fail("result is expected")

        // when
        val actual = orderService.getOrdersByStatus(OrderStatusDTO.CREATED)

        // then
        assertThat(actual[0].id).isEqualTo(created.id)
        assertThat(actual[0].orderItems.iterator().next().productId).isEqualTo(1)
        assertThat(actual[0].orderItems.iterator().next().quantity).isEqualTo(3)
        assertThat(actual[0].orderStatus).isEqualTo(OrderStatusDTO.CREATED)
    }

    @Test
    fun `should get all orders`() {

        // given
        transactionTemplate.execute {
            orderRepository.deleteAll()
        }

        // and
        transactionTemplate.execute {
            orderRepository.saveAll(
                listOf(
                    Order(
                        orderItems = setOf(
                            OrderItem(
                                productId = 1,
                                quantity = 2
                            ),
                        )
                    ),
                    Order(
                        orderItems = setOf(
                            OrderItem(
                                productId = 3,
                                quantity = 4
                            ),
                        )
                    )
                )
            )
        }

        // when
        val orders = orderService.getOrders()

        // then
        assertThat(orders).hasSize(2)
    }

    @Test
    fun `should update order`() {

        // given
        val orderToChange = transactionTemplate.execute {
            orderRepository.save(
                Order(
                    orderItems = setOf(
                        OrderItem(
                            productId = 1L,
                            quantity = 3
                        )
                    ),
                )
            )
        }?.toOrderResponseDTO() ?: fail("result is expected")

        // and
        val updateOrderRequest = UpdateOrderRequest(
            orderItems = listOf(
                OrderItemDTO(
                    productId = 1,
                    quantity = 4
                )
            )
        )

        // when
        val changedOrder = transactionTemplate.execute {
            orderService.updateOrder(orderToChange.id, updateOrderRequest)
        } ?: fail("result is expected")

        // then
        assertThat(changedOrder.orderItems.iterator().next().quantity).isEqualTo(4)
        assertThat(changedOrder.orderItems.iterator().next().productId).isEqualTo(1)
        assertThat(changedOrder.orderStatus).isEqualTo(OrderStatusDTO.UPDATED)
        assertThat(changedOrder.createdAt).isBeforeOrEqualTo(LocalDateTime.now())
        assertThat(changedOrder.updatedAt).isAfterOrEqualTo(changedOrder.createdAt)
    }

    @Test
    fun `should change order status`() {

        // given
        val orderToChange = transactionTemplate.execute {
            orderRepository.save(
                Order(
                    orderItems = setOf(
                        OrderItem(
                            productId = 1L,
                            quantity = 3
                        )
                    ),
                    orderStatus = OrderStatus.CREATED
                )
            )
        }?.toOrderResponseDTO() ?: fail("result is expected")

        // when
        val changedOrder = transactionTemplate.execute {
            orderService.changeOrderStatus(orderToChange.id, OrderStatusDTO.BOOKED)
        } ?: fail("result is expected")

        // then
        assertThat(changedOrder.id).isEqualTo(orderToChange.id)
        assertThat(changedOrder.orderStatus).isEqualTo(OrderStatusDTO.BOOKED)
    }

    @Test
    fun `should not update order without orderItems`() {

        // given
        val orderToUpdate = transactionTemplate.execute {
            orderRepository.save(
                Order(
                    orderItems = setOf(
                        OrderItem(
                            productId = 1L,
                            quantity = 3
                        )
                    ),
                )
            )
        }?.toOrderResponseDTO() ?: fail("result is expected")

        // when
        val exception = assertThrows<InsufficientAmountOfOrderItemException> {
            orderService.updateOrder(orderToUpdate.id, UpdateOrderRequest(listOf()))
        }

        // then
        assertThat(exception.message).contains("You have to add minimum one order item")
    }

    @Test
    fun `should not update non existing order`() {
        // given
        val nonExitingId = -15L

        // and
        val request = UpdateOrderRequest(listOf(OrderItemDTO()))

        // when
        val exception = assertThrows<OrderNotFoundException> {
            orderService.updateOrder(nonExitingId, request)
        }

        // then
        assertThat(exception.message).contains("Order", "was not found")
    }

    @Test
    fun `should get order details`() {

        // given
        val order = transactionTemplate.execute {
            orderRepository.save(
                Order(
                    orderStatus = OrderStatus.CREATED,
                    orderItems = setOf(
                        OrderItem(
                            productId = 1,
                            quantity = 1
                        ),
                        OrderItem(
                            productId = 2,
                            quantity = 2
                        )
                    )
                )
            )
        } ?: fail("result is expected")

        // given
        stubFor(
            get("/api/v1/products/details?ids=1&ids=2")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(
                    aResponse()
                        .withBody(
                            objectMapper
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(
                                    listOf(
                                        ProductDetailsResponse(
                                            id = 1,
                                            name = "test1",
                                            quantity = 3,
                                            price = BigDecimal.ONE
                                        ),
                                        ProductDetailsResponse(
                                            id = 2,
                                            name = "test2",
                                            quantity = 4,
                                            price = BigDecimal.TEN
                                        )
                                    )
                                )
                        )
                )
        )

        // when
        val orderDetails = orderService.getOrderDetails(order.id ?: -1)

        // then
        assertThat(orderDetails.orderItemDetails).hasSize(2)
        assertThat(orderDetails.orderItemDetails.iterator().next().productId).isEqualTo(1)
        assertThat(orderDetails.orderItemDetails.iterator().next().name).isEqualTo("test1")
        assertThat(orderDetails.orderItemDetails.iterator().next().quantity).isEqualTo(1)
        assertThat(orderDetails.orderItemDetails.iterator().next().price).isEqualTo(BigDecimal.ONE)
        assertThat(orderDetails.total).isEqualTo(BigDecimal("21").setScale(2)) // price multiply quantity
    }

    @Test
    fun `should not get order details for order with status None`() {


        // given
        val order = transactionTemplate.execute {
            orderRepository.save(
                Order(
                    orderStatus = OrderStatus.NONE,
                    orderItems = setOf(
                        OrderItem(
                            productId = 1,
                            quantity = 1
                        ),
                    )
                )
            )
        } ?: fail("result is expected")

        // when
        val exception = assertThrows<OrderNotCreatedException> {
            orderService.getOrderDetails(order.id ?: -1)
        }

        // then
        assertThat(exception.message).contains("Order must be created")

    }

    @Test
    fun `should confirm order`() {

        // setup
        val accountancyServer = WireMockServer(8084)
        accountancyServer.start()

        // given
        val order = transactionTemplate.execute {
            orderRepository.save(
                Order(
                    orderStatus = OrderStatus.CREATED,
                    orderItems = setOf(
                        OrderItem(
                            productId = 1,
                            quantity = 1
                        ),
                    )
                )
            )
        } ?: fail("result is expected")

        // and
        accountancyServer.stubFor(
            post("/api/v1/accountancy/invoice/outcome")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
                .withRequestBody(
                    EqualToPattern(
                        objectMapper
                            .writerWithDefaultPrettyPrinter()
                            .writeValueAsString(
                                listOf(
                                    OrderItemDTO(
                                        productId = 1,
                                        quantity = 1
                                    )
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
                                    InvoiceResponse(
                                        id = 1,
                                        orderId = order.id ?: -1,
                                        status = InvoiceStatusDTO.CREATED,
                                    )
                                )
                        )
                )
        )

        // when
        val invoice = orderService.confirmOrder(order.id ?: -1)

        // then
        assertThat(invoice.id).isNotNull
        assertThat(invoice.orderId).isEqualTo(order.id ?: -1)
        assertThat(invoice.status).isEqualTo(InvoiceStatusDTO.CREATED)
        accountancyServer.stop()
    }

    @Test
    fun `should not confirm order if internal server is unavailable`() {

        // given
        val order = transactionTemplate.execute {
            orderRepository.save(
                Order(
                    orderStatus = OrderStatus.CREATED,
                    orderItems = setOf(
                        OrderItem(
                            productId = 1,
                            quantity = 1
                        ),
                    )
                )
            )
        } ?: fail("result is expected")

        // when
        val exception = assertThrows<AccountancyServerNotAvailable> {
            orderService.confirmOrder(order.id ?: -1)
        }

        // then
        assertThat(exception.message).contains("We can't create invoice. Try again later")
    }

    @Test
    fun `should not confirm order if orderStatus is not CREATED or UPDATED `() {

        // given
        val order = transactionTemplate.execute {
            orderRepository.save(
                Order(
                    orderStatus = OrderStatus.CONFIRMED,
                    orderItems = setOf(
                        OrderItem(
                            productId = 1,
                            quantity = 1
                        ),
                    )
                )
            )
        } ?: fail("result is expected")

        // when
        val exception = assertThrows<OrderStatusException> {
            orderService.confirmOrder(order.id ?: -1)
        }

        // then
        assertThat(exception.message).contains("Order is already confirmed or cancelled")
    }

    @Test
    fun `should not confirm empty order `() {

        // given
        val order = transactionTemplate.execute {
            orderRepository.save(
                Order(
                    orderStatus = OrderStatus.CONFIRMED,
                )
            )
        } ?: fail("result is expected")

        // when
        val exception = assertThrows<InsufficientAmountOfOrderItemException> {
            orderService.confirmOrder(order.id ?: -1)
        }

        // then
        assertThat(exception.message).contains("You have to add minimum one order item")
    }

    @Test
    fun `should get productId to quantity sorted by quantity`() {

        // given
        transactionTemplate.execute {
            orderRepository.deleteAll()
        } ?: fail("result is expected")

        // and
        transactionTemplate.execute {
            orderRepository.saveAll(
                listOf(
                    Order(
                        orderStatus = OrderStatus.PAID,
                        orderItems = setOf(
                            OrderItem(
                                productId = 3,
                                quantity = 2
                            ),
                        )
                    ),
                    Order(
                        orderStatus = OrderStatus.PAID,
                        orderItems = setOf(
                            OrderItem(
                                productId = 5,
                                quantity = 7
                            ),
                        )
                    ),
                    Order(
                        orderStatus = OrderStatus.PAID,
                        orderItems = setOf(
                            OrderItem(
                                productId = 1,
                                quantity = 4
                            ),
                            OrderItem(
                                productId = 5,
                                quantity = 2
                            )
                        )
                    ),
                    Order(
                        orderStatus = OrderStatus.CREATED,
                        orderItems = setOf(
                            OrderItem(
                                productId = 2,
                                quantity = 5
                            )
                        )
                    )
                )
            )
        } ?: fail("result is expected")

        // when
        val sortedItems = orderService.getProductsIdToQuantity()
        // then
        assertThat(sortedItems).hasSize(3)
        assertThat(sortedItems.iterator().next().value).isEqualTo(9)
        assertThat(sortedItems[1]).isEqualTo(4)
        assertThat(sortedItems[3]).isEqualTo(2)
        assertThat(sortedItems[5]).isEqualTo(9)
    }
}
