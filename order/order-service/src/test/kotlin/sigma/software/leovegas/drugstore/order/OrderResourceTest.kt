package sigma.software.leovegas.drugstore.order

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
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.infrastructure.extensions.respTypeRef
import sigma.software.leovegas.drugstore.order.api.CreateOrderRequest
import sigma.software.leovegas.drugstore.order.api.CreateOrderResponse
import sigma.software.leovegas.drugstore.order.api.OrderItemDTO
import sigma.software.leovegas.drugstore.order.api.UpdateOrderRequest
import sigma.software.leovegas.drugstore.order.api.UpdateOrderResponse

@DisplayName("OrderResource test")
@SpringBootTest(webEnvironment = RANDOM_PORT)
class OrderResourceTest(
    @Autowired val restTemplate: TestRestTemplate,
    @Autowired val transactionTemplate: TransactionTemplate,
    @Autowired val orderRepository: OrderRepository,
    @Autowired val orderService: OrderService
) {

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
        val response = restTemplate.exchange("/api/v1/orders", POST, httpEntity, respTypeRef<CreateOrderResponse>())

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
        }?.toCreateOrderResponseDTO() ?: fail("result is expected")

        // when
        val response = restTemplate
            .exchange("/api/v1/orders/${orderCreated.id}", GET, null, respTypeRef<CreateOrderResponse>())

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        // and
        val body = response.body ?: fail("body may not be null")
        assertThat(body.id).isEqualTo(orderCreated.id)
        assertThat(body.orderItems.elementAt(0).quantity).isEqualTo(orderCreated.orderItems.elementAt(0).quantity)
        assertThat(body.orderItems.elementAt(0).productId).isEqualTo(orderCreated.orderItems.elementAt(0).productId)

    }

    @Test
    fun `should get orders`() {

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
        }?.toCreateOrderResponseDTO() ?: fail("result is expected")

        // when
        val response = restTemplate
            .exchange("/api/v1/orders", GET, null, respTypeRef<List<CreateOrderResponse>>())

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        // and
        val body = response.body ?: fail("body may not be null")
        assertThat(body).isNotNull
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
        }?.toUpdateOrderResponseDTO() ?: fail("result is expected")

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
            .exchange("/api/v1/orders/${orderCreated.id}", PUT, httpEntity, respTypeRef<UpdateOrderResponse>())

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.ACCEPTED)

        // and
        val body = response.body ?: fail("body may not be null")
        assertThat(body).isNotNull
        assertThat(body.id).isEqualTo(orderCreated.id)
        assertThat(body.orderItems.elementAt(0).quantity).isEqualTo(httpEntity.body?.orderItems?.elementAt(0)?.quantity)
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
        }?.toCreateOrderResponseDTO() ?: fail("result is expected")

        // when
        val response = restTemplate
            .exchange("/api/v1/orders/${orderCreated.id}", DELETE, null, respTypeRef<CreateOrderResponse>())

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.NO_CONTENT)

        // and
        assertThrows<OrderNotFoundException> {
            transactionTemplate.execute {
                orderService.getOrderById(orderCreated.id!!)
            }
        }
    }
}
