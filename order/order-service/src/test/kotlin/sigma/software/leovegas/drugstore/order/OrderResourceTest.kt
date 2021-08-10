package sigma.software.leovegas.drugstore.order

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.matching.ContainsPattern
import java.math.BigDecimal
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.fail
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod.DELETE
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpMethod.POST
import org.springframework.http.HttpMethod.PUT
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.infrastructure.extensions.respTypeRef
import sigma.software.leovegas.drugstore.order.api.CreateOrderRequest
import sigma.software.leovegas.drugstore.order.api.OrderDetailsDTO
import sigma.software.leovegas.drugstore.order.api.OrderItemDTO
import sigma.software.leovegas.drugstore.order.api.OrderResponse
import sigma.software.leovegas.drugstore.order.api.UpdateOrderRequest
import sigma.software.leovegas.drugstore.product.api.ProductResponse

@DisplayName("OrderResource test")
@SpringBootTest(webEnvironment = RANDOM_PORT)
class OrderResourceTest @Autowired constructor(
    val restTemplate: TestRestTemplate,
    val transactionTemplate: TransactionTemplate,
    val orderRepository: OrderRepository,
    val orderService: OrderService,
    val objectMapper: ObjectMapper,
) : WireMockTest() {

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
        val response = restTemplate.exchange("/api/v1/orders", POST, httpEntity, respTypeRef<OrderResponse>())

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)

        // and
        val body = response.body ?: fail("body may not be null")
        assertThat(body.id).isNotNull
        assertThat(body.orderItems).hasSize(1)
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
            .exchange("/api/v1/orders/${orderCreated.id}", GET, null, respTypeRef<OrderResponse>())

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        // and
        val body = response.body ?: fail("body may not be null")
        assertThat(body.id).isEqualTo(orderCreated.id)
        assertThat(body.orderItems.iterator().next().productId).isEqualTo(1)
        assertThat(body.orderItems.iterator().next().quantity).isEqualTo(3)

    }

    @Test
    fun `should get orderDetails`() {

        // setup
        stubFor(
            get("/api/v1/products-by-ids/?ids=1")
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
                                            price = BigDecimal.TEN
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
                            productId = 1L,
                            quantity = 3
                        )
                    )
                )
            )
        } ?: fail("result is expected")

        // when
        val response = restTemplate
            .exchange("/api/v1/orders/${order.id}/details", GET, null, respTypeRef<OrderDetailsDTO>())

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        // and
        val body = response.body ?: fail("body may not be null")
        assertThat(body.orderItemDetails).hasSize(1)
        assertThat(body.orderItemDetails.iterator().next().name).isEqualTo("test1")
        assertThat(body.orderItemDetails.iterator().next().price).isEqualTo(BigDecimal.TEN)
        assertThat(body.orderItemDetails.iterator().next().quantity).isEqualTo(3)
        assertThat(body.total).isEqualTo(BigDecimal("30").setScale(2)) // price multiply quantity

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
            .exchange("/api/v1/orders", GET, null, respTypeRef<List<OrderResponse>>())

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
            .exchange("/api/v1/orders/${orderCreated.id}", PUT, httpEntity, respTypeRef<OrderResponse>())

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.ACCEPTED)

        // and
        val body = response.body ?: fail("body may not be null")
        assertThat(body).isNotNull
        assertThat(body.orderItems.iterator().next().quantity).isEqualTo(5)
    }

    @Test
    fun `should delete order`() {

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
                )
            )
        }?.toOrderResponseDTO() ?: fail("result is expected")

        // when
        val response = restTemplate
            .exchange("/api/v1/orders/${orderCreated.id}", DELETE, null, respTypeRef<OrderResponse>())

        // and
        val exception = assertThrows<OrderNotFoundException> {
            transactionTemplate.execute {
                orderService.getOrderById(orderCreated.id)
            }
        }

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.NO_CONTENT)

        // and
        assertThat(exception.message).contains("Order", "not found")
    }
}
