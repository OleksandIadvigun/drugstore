package sigma.software.leovegas.drugstore.order

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.matching.ContainsPattern
import com.github.tomakehurst.wiremock.matching.EqualToPattern
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
import sigma.software.leovegas.drugstore.accountancy.api.CreateOutcomeInvoiceRequest
import sigma.software.leovegas.drugstore.accountancy.api.InvoiceResponse
import sigma.software.leovegas.drugstore.accountancy.api.InvoiceStatusDTO
import sigma.software.leovegas.drugstore.accountancy.api.ItemDTO
import sigma.software.leovegas.drugstore.infrastructure.extensions.respTypeRef
import sigma.software.leovegas.drugstore.order.api.CreateOrderRequest
import sigma.software.leovegas.drugstore.order.api.OrderDetailsDTO
import sigma.software.leovegas.drugstore.order.api.OrderItemDTO
import sigma.software.leovegas.drugstore.order.api.OrderResponse
import sigma.software.leovegas.drugstore.order.api.OrderStatusDTO
import sigma.software.leovegas.drugstore.order.api.UpdateOrderRequest
import sigma.software.leovegas.drugstore.product.api.SearchProductResponse

@DisplayName("Order Resource test")
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
                        productId = 1L,
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
        assertThat(body.orderItems[0].productId).isEqualTo(1L)
        assertThat(body.orderItems[0].quantity).isEqualTo(3)
        assertThat(body.orderStatus).isEqualTo(OrderStatusDTO.CREATED)
        assertThat(body.createdAt).isBeforeOrEqualTo(LocalDateTime.now())
        assertThat(body.updatedAt).isBeforeOrEqualTo(LocalDateTime.now())
    }

    @Test
    fun `should get order by id`() {

        // given
        val orderCreated = transactionTemplate.execute {
            orderRepository.save(
                Order(
                    orderItems = setOf(
                        OrderItem(
                            productId = 1L,
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
        assertThat(body.orderItems.iterator().next().productId).isEqualTo(1)
        assertThat(body.orderItems.iterator().next().quantity).isEqualTo(3)
        assertThat(body.createdAt).isBeforeOrEqualTo(LocalDateTime.now())
        assertThat(body.updatedAt).isAfterOrEqualTo(body.createdAt)
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
                            productId = 1L,
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
        assertThat(body[0].orderItems.iterator().next().productId).isEqualTo(1)
        assertThat(body[0].orderItems.iterator().next().quantity).isEqualTo(3)
        assertThat(body[0].orderStatus).isEqualTo(OrderStatusDTO.CREATED)
    }

    @Test
    fun `should get orderDetails`() {

        // setup
        val wireMockProductServer = WireMockServer(8081)
        wireMockProductServer.start()

        // and
        wireMockProductServer.stubFor(
            get("/api/v1/products/details?ids=1&ids=2")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(
                    aResponse()
                        .withBody(
                            objectMapper
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(
                                    listOf(
                                        SearchProductResponse(
                                            id = 1L,
                                            name = "test1",
                                            quantity = 3,
                                            price = BigDecimal("20.00"),
                                        ),
                                        SearchProductResponse(
                                            id = 2L,
                                            name = "test2",
                                            quantity = 4,
                                            price = BigDecimal("30.00")
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
                    orderStatus = OrderStatus.CREATED,
                    orderItems = setOf(
                        OrderItem(
                            productId = 1L,
                            quantity = 1
                        ),
                        OrderItem(
                            productId = 2L,
                            quantity = 2
                        )
                    )
                )
            )
        } ?: fail("result is expected")

        // when
        val response = restTemplate
            .exchange("$baseUrl/api/v1/orders/${order.id}/details", GET, null, respTypeRef<OrderDetailsDTO>())
        wireMockProductServer.stop()

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        // and
        val body = response.body ?: fail("body may not be null")
        assertThat(body.orderItemDetails).hasSize(2)
        assertThat(body.orderItemDetails.iterator().next().name).isEqualTo("test1")
        assertThat(body.orderItemDetails.iterator().next().productId).isEqualTo(1)
        assertThat(body.orderItemDetails.iterator().next().quantity).isEqualTo(1)
        assertThat(body.orderItemDetails.iterator().next().price).isEqualTo(BigDecimal("20.00"))
        assertThat(body.total).isEqualTo((BigDecimal("80").setScale(2)))
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
            WireMock.post("/api/v1/accountancy/invoice/outcome")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
                .withRequestBody(
                    EqualToPattern(
                        objectMapper
                            .writerWithDefaultPrettyPrinter()
                            .writeValueAsString(
                                CreateOutcomeInvoiceRequest(
                                    listOf(
                                        ItemDTO(
                                            productId = 1,
                                            quantity = 1
                                        )
                                    ), order.id ?: -1
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

        // and
        val http = HttpEntity(order.id ?: -1)

        // when
        val invoice = restTemplate
            .exchange("$baseUrl/api/v1/orders/confirm", POST, http, respTypeRef<InvoiceResponse>())
        accountancyServer.stop()

        // then
        assertThat(invoice.body?.id).isNotNull
        assertThat(invoice.body?.orderId).isEqualTo(order.id ?: -1)
        assertThat(invoice.body?.status).isEqualTo(InvoiceStatusDTO.CREATED)
        accountancyServer.stop()
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
                            productId = 1L,
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
                        productId = 1L,
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
        assertThat(body.orderItems.iterator().next().productId).isEqualTo(1)
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
                            productId = 1L,
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
        assertThat(body.orderItems.iterator().next().productId).isEqualTo(1)
        assertThat(body.orderStatus).isEqualTo(OrderStatusDTO.BOOKED)
    }

    @Test
    fun `should get productId to quantity sorted by quantity `() {

        // given
        orderRepository.deleteAll()


        // and
        transactionTemplate.execute {
            orderRepository.saveAll(
                listOf(
                    Order(
                        orderStatus = OrderStatus.PAID,
                        orderItems = setOf(
                            OrderItem(
                                productId = 4,
                                quantity = 3
                            )
                        ),
                    ),
                    Order(
                        orderStatus = OrderStatus.PAID,
                        orderItems = setOf(
                            OrderItem(
                                productId = 5,
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
