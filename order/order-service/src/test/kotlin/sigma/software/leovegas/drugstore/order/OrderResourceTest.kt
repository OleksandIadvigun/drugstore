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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpMethod.POST
import org.springframework.http.HttpMethod.PUT
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.accountancy.api.PriceItemResponse
import sigma.software.leovegas.drugstore.infrastructure.extensions.respTypeRef
import sigma.software.leovegas.drugstore.order.api.CreateOrderRequest
import sigma.software.leovegas.drugstore.order.api.OrderDetailsDTO
import sigma.software.leovegas.drugstore.order.api.OrderItemDTO
import sigma.software.leovegas.drugstore.order.api.OrderResponse
import sigma.software.leovegas.drugstore.order.api.OrderStatusDTO
import sigma.software.leovegas.drugstore.order.api.UpdateOrderRequest
import sigma.software.leovegas.drugstore.product.api.ProductResponse

@DisplayName("OrderResource test")
@SpringBootTest(webEnvironment = RANDOM_PORT)
class OrderResourceTest @Autowired constructor(
    @LocalServerPort val port: Int,
    val orderRepository: OrderRepository,
    val objectMapper: ObjectMapper,
    val orderProperties: OrderProperties,
    val restTemplate: TestRestTemplate,
    val transactionTemplate: TransactionTemplate,
) {

    lateinit var baseUrl: String

    @BeforeEach
    fun setup() {
        baseUrl = "http://${orderProperties.host}:$port"
    }

    @Test
    fun `should create order`() {

        // given
        val httpEntity = HttpEntity(
            CreateOrderRequest(
                listOf(
                    OrderItemDTO(
                        priceItemId = 1L,
                        quantity = 3
                    )
                )
            )
        )

        // when
        val response = restTemplate.exchange("$baseUrl/api/v1/orders", POST, httpEntity, respTypeRef<OrderResponse>())

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)

        // and
        val body = response.body ?: fail("body may not be null")
        assertThat(body.id).isNotNull
        assertThat(body.orderItems).hasSize(1)
        assertThat(body.orderItems[0].priceItemId).isEqualTo(1L)
        assertThat(body.orderItems[0].quantity).isEqualTo(3)
        assertThat(body.orderStatus).isEqualTo(OrderStatusDTO.CREATED)
        assertThat(body.createdAt).isBefore(LocalDateTime.now())
        assertThat(body.updatedAt).isBefore(LocalDateTime.now())
    }

    @Test
    fun `should get order by id`() {

        // given
        val orderCreated = transactionTemplate.execute {
            orderRepository.save(
                Order(
                    orderItems = setOf(
                        OrderItem(
                            priceItemId = 1L,
                            quantity = 3
                        )
                    )
                )
            )
        }?.toOrderResponseDTO() ?: fail("result is expected")

        // when
        val response = restTemplate
            .exchange("$baseUrl/api/v1/orders/${orderCreated.id}", GET, null, respTypeRef<OrderResponse>())

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        // and
        val body = response.body ?: fail("body may not be null")
        assertThat(body.id).isEqualTo(orderCreated.id)
        assertThat(body.orderItems.iterator().next().priceItemId).isEqualTo(1)
        assertThat(body.orderItems.iterator().next().quantity).isEqualTo(3)
        assertThat(body.createdAt).isBefore(LocalDateTime.now())
        assertThat(body.updatedAt).isBefore(LocalDateTime.now())

    }

    @Test
    fun `should get order by status`() {

        // given
        transactionTemplate.execute {
            orderRepository.deleteAll()
        }

        // given
        val orderCreated = transactionTemplate.execute {
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
        val response = restTemplate
            .exchange(
                "$baseUrl/api/v1/orders/status/${orderCreated.orderStatus}", GET,
                null, respTypeRef<List<OrderResponse>>()
            )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        // and
        val body = response.body ?: fail("body may not be null")
        assertThat(body[0].id).isEqualTo(orderCreated.id)
        assertThat(body[0].orderItems.iterator().next().priceItemId).isEqualTo(1)
        assertThat(body[0].orderItems.iterator().next().quantity).isEqualTo(3)
        assertThat(body[0].orderStatus).isEqualTo(OrderStatusDTO.CREATED)
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
            get("/api/v1/accountancy/price-items-by-ids/ids=1,2&markup=true")
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
        val response = restTemplate
            .exchange("$baseUrl/api/v1/orders/${order.id}/details", GET, null, respTypeRef<OrderDetailsDTO>())
        wireMockServer8084.stop()
        wireMockServer8081.stop()

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        // and
        val body = response.body ?: fail("body may not be null")
        assertThat(body.orderItemDetails).hasSize(2)
        assertThat(body.orderItemDetails.iterator().next().name).isEqualTo("test1")
        assertThat(body.orderItemDetails.iterator().next().priceItemId).isEqualTo(1)
        assertThat(body.orderItemDetails.iterator().next().quantity).isEqualTo(1)
        assertThat(body.orderItemDetails.iterator().next().price).isEqualTo(BigDecimal("20.00"))
        assertThat(body.total).isEqualTo((BigDecimal("100").setScale(2)))
    }

    @Test
    fun `should get orders`() {

        // given
        transactionTemplate.execute {
            orderRepository.deleteAll()
        }

        // and
        val orderCreated = transactionTemplate.execute {
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
        val response = restTemplate
            .exchange("$baseUrl/api/v1/orders", GET, null, respTypeRef<List<OrderResponse>>())

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        // and
        val body = response.body ?: fail("body may not be null")
        assertThat(body).isNotNull
        assertThat(body).hasSize(2)
    }

    @Test
    fun `should update order`() {

        // given
        val orderCreated = transactionTemplate.execute {
            orderRepository.save(
                Order(
                    orderItems = setOf(
                        OrderItem(
                            priceItemId = 1L,
                            quantity = 3
                        )
                    )
                )
            )
        }?.toOrderResponseDTO() ?: fail("result is expected")

        // and
        val httpEntity = HttpEntity(
            UpdateOrderRequest(
                listOf(
                    OrderItemDTO(
                        priceItemId = 1L,
                        quantity = 5
                    )
                )
            )
        )

        // when
        val response = restTemplate
            .exchange("$baseUrl/api/v1/orders/${orderCreated.id}", PUT, httpEntity, respTypeRef<OrderResponse>())

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.ACCEPTED)

        // and
        val body = response.body ?: fail("body may not be null")
        assertThat(body).isNotNull
        assertThat(body.orderItems.iterator().next().quantity).isEqualTo(5)
        assertThat(body.orderItems.iterator().next().priceItemId).isEqualTo(1)
        assertThat(body.orderStatus).isEqualTo(OrderStatusDTO.UPDATED)
        assertThat(body.createdAt).isBefore(LocalDateTime.now())
        assertThat(body.updatedAt).isAfter(body.createdAt)
    }

    @Test
    fun `should change order status`() {

        // given
        val orderCreated = transactionTemplate.execute {
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

        // and
        val httpEntity = HttpEntity(
            OrderStatus.BOOKED
        )

        // when
        val response = restTemplate
            .exchange(
                "$baseUrl/api/v1/orders/change-status/${orderCreated.id}", PUT,
                httpEntity, respTypeRef<OrderResponse>()
            )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.ACCEPTED)

        // and
        val body = response.body ?: fail("body may not be null")
        assertThat(body).isNotNull
        assertThat(body.orderItems.iterator().next().quantity).isEqualTo(3)
        assertThat(body.orderItems.iterator().next().priceItemId).isEqualTo(1)
        assertThat(body.orderStatus).isEqualTo(OrderStatusDTO.BOOKED)
    }

    @Test
    fun `should return total buys from items sorted DESC by quantity `() {

        // given
        orderRepository.deleteAll()


        // and
        transactionTemplate.execute {
            orderRepository.saveAll(
                listOf(
                    Order(
                        orderItems = setOf(
                            OrderItem(
                                priceItemId = 4,
                                quantity = 3
                            )
                        ),
                    ),
                    Order(
                        orderItems = setOf(
                            OrderItem(
                                priceItemId = 5,
                                quantity = 5
                            )
                        ),
                    )
                )
            )
        } ?: fail("result is expected")

        // when
        val response = restTemplate
            .exchange("$baseUrl/api/v1/orders/total-buys", GET, null, respTypeRef<Map<Long, Int>>())

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.size).isEqualTo(2)
        assertThat(response.body?.iterator()?.next()?.value).isEqualTo(5) // first should have the biggest value
        assertThat(response.body?.get(4)).isEqualTo(3)
    }
}
