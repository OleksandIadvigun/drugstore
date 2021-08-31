package sigma.software.leovegas.drugstore.order

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.matching.ContainsPattern
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
import org.springframework.http.MediaType
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.accountancy.api.PriceItemResponse
import sigma.software.leovegas.drugstore.order.api.CreateOrderRequest
import sigma.software.leovegas.drugstore.order.api.OrderItemDTO
import sigma.software.leovegas.drugstore.order.api.OrderStatusDTO
import sigma.software.leovegas.drugstore.order.api.UpdateOrderRequest
import sigma.software.leovegas.drugstore.product.api.ProductResponse

@AutoConfigureTestDatabase
@DisplayName("OrderService test")
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
                    priceItemId = 1,
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
        assertThat(created.orderItems[0].priceItemId).isEqualTo(1)
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
        assertThat(exception.message).contains("You have to add minimum one order item to make the order")
    }

    @Test
    fun `should get order by id `() {

        // given
        val created = transactionTemplate.execute {
            orderRepository.save(
                Order(
                    orderItems = setOf(
                        OrderItem(
                            priceItemId = 1,
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
        assertThat(actual.orderItems.iterator().next().priceItemId).isEqualTo(1)
        assertThat(actual.orderItems.iterator().next().quantity).isEqualTo(3)
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
                            priceItemId = 1,
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
        assertThat(actual[0].orderItems.iterator().next().priceItemId).isEqualTo(1)
        assertThat(actual[0].orderItems.iterator().next().quantity).isEqualTo(3)
        assertThat(actual[0].orderStatus).isEqualTo(OrderStatusDTO.CREATED)
    }

    @Test
    fun `should get orderDetails`() {

        // setup
        val wireMockServer8081 = WireMockServer(8081)
        val wireMockServer8084 = WireMockServer(8084)
        wireMockServer8081.start()
        wireMockServer8084.start()

        // and
        wireMockServer8081.stubFor(
            get("/api/v1/products-by-ids/?ids=1&ids=2")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(
                    aResponse()
                        .withBody(
                            objectMapper
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(
                                    listOf(
                                        ProductResponse(
                                            id = 1L,
                                            name = "test1",
                                        ),
                                        ProductResponse(
                                            id = 2L,
                                            name = "test2",
                                        )
                                    )
                                )
                        )
                )
        )

        // and
        wireMockServer8084.stubFor(
            get("/api/v1/accountancy/price-items-by-ids?ids=1&ids=2&markup=true")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(
                    aResponse()
                        .withBody(
                            objectMapper
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(
                                    listOf(
                                        PriceItemResponse(
                                            id = 1,
                                            productId = 1,
                                            price = BigDecimal("20.00")
                                        ),
                                        PriceItemResponse(
                                            id = 2,
                                            productId = 2,
                                            price = BigDecimal("40.00")
                                        )
                                    )
                                )
                        )
                )
        )

        // and
        val order = transactionTemplate.execute {
            orderRepository.save(
                Order(
                    orderItems = setOf(
                        OrderItem(
                            priceItemId = 1L,
                            quantity = 1
                        ),
                        OrderItem(
                            priceItemId = 2L,
                            quantity = 2
                        )
                    )
                )
            )
        } ?: fail("result is expected")

        // when
        val orderDetails = orderService.getOrderDetails(order.id ?: -1)
        wireMockServer8084.stop()
        wireMockServer8081.stop()

        // then
        assertThat(orderDetails.orderItemDetails).hasSize(2)
        assertThat(orderDetails.orderItemDetails.iterator().next().name).isEqualTo("test1")
        assertThat(orderDetails.orderItemDetails.iterator().next().priceItemId).isEqualTo(1)
        assertThat(orderDetails.orderItemDetails.iterator().next().quantity).isEqualTo(1)
        assertThat(orderDetails.total).isEqualTo(BigDecimal("100").setScale(2)) // price multiply quantity
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
                                priceItemId = 1,
                                quantity = 2
                            ),
                        )
                    ),
                    Order(
                        orderItems = setOf(
                            OrderItem(
                                priceItemId = 3,
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
                            priceItemId = 1L,
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
                    priceItemId = 1,
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
        assertThat(changedOrder.orderItems.iterator().next().priceItemId).isEqualTo(1)
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
                            priceItemId = 1L,
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
                            priceItemId = 1L,
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
        assertThat(exception.message).contains("You have to add minimum one order item to make the order")
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
                                priceItemId = 3,
                                quantity = 2
                            ),
                        )
                    ),
                    Order(
                        orderStatus = OrderStatus.PAID,
                        orderItems = setOf(
                            OrderItem(
                                priceItemId = 5,
                                quantity = 7
                            ),
                        )
                    ),
                    Order(
                        orderStatus = OrderStatus.PAID,
                        orderItems = setOf(
                            OrderItem(
                                priceItemId = 1,
                                quantity = 4
                            ),
                            OrderItem(
                                priceItemId = 5,
                                quantity = 2
                            )
                        )
                    ),
                    Order(
                        orderStatus = OrderStatus.CREATED,
                        orderItems = setOf(
                            OrderItem(
                                priceItemId = 2,
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
