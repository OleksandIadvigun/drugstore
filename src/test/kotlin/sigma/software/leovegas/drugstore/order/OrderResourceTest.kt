package sigma.software.leovegas.drugstore.order

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
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.infrastructure.extensions.respTypeRef
import sigma.software.leovegas.drugstore.product.Product
import sigma.software.leovegas.drugstore.product.ProductRepository

@DisplayName("OrderResource test")
@SpringBootTest(webEnvironment = RANDOM_PORT)
class OrderResourceTest(
    @Autowired val restTemplate: TestRestTemplate,
    @Autowired val transactionTemplate: TransactionTemplate,
    @Autowired val orderRepository: OrderRepository,
    @Autowired val productRepository: ProductRepository,
    @Autowired val orderService: OrderService
) {

    @Test
    fun `should create order`() {

        // given
        val product = transactionTemplate.execute {
            productRepository.save(
                Product(
                    name = "test product",
                    price = BigDecimal.TEN.setScale(2),
                )
            )
        } ?: fail("result is expected")

        // and
        val httpEntity = HttpEntity(
            OrderRequest(
                setOf(
                    OrderItem(
                        productId = product.id!!,
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
        val product = transactionTemplate.execute {
            productRepository.save(
                Product(
                    name = "test product",
                    price = BigDecimal.TEN.setScale(2),
                )
            )
        } ?: fail("result is expected")

        // and
        val orderCreated = transactionTemplate.execute {
            orderRepository.save(
                Order(
                    orderItems = setOf(
                        OrderItem(
                            productId = product.id!!,
                            quantity = 3
                        )
                    )
                )
            )
        }?.toOrderResponse() ?: fail("result is expected")

        // when
        val response = restTemplate
            .exchange("/api/v1/orders/${orderCreated.id}", GET, null, respTypeRef<OrderResponse>())

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

        // when
        val response = restTemplate
            .exchange("/api/v1/orders", GET, null, respTypeRef<List<OrderResponse>>())

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        // and
        val body = response.body ?: fail("body may not be null")
        assertThat(body).isNotNull
    }

    @Test
    fun `should update order`() {

        // given
        val product = transactionTemplate.execute {
            productRepository.save(
                Product(
                    name = "test product",
                    price = BigDecimal.TEN.setScale(2),
                )
            )
        } ?: fail("result is expected")

        // and
        val orderCreated = transactionTemplate.execute {
            orderRepository.save(
                Order(
                    orderItems = setOf(
                        OrderItem(
                            productId = product.id!!,
                            quantity = 3
                        )
                    )
                )
            )
        }?.toOrderResponse() ?: fail("result is expected")

        // and
        val httpEntity = HttpEntity(
            OrderRequest(
                setOf(
                    OrderItem(
                        productId = product.id!!,
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
        assertThat(body.id).isEqualTo(orderCreated.id)
        assertThat(body.orderItems.elementAt(0).quantity).
        isEqualTo(httpEntity.body?.orderItems?.elementAt(0)?.quantity)
    }

    @Test
    fun `should delete order`() {

        // given
        val product = transactionTemplate.execute {
            productRepository.save(
                Product(
                    name = "test product",
                    price = BigDecimal.TEN.setScale(2),
                )
            )
        } ?: fail("result is expected")

        // and
        val orderCreated = transactionTemplate.execute {
            orderRepository.save(
                Order(
                    orderItems = setOf(
                        OrderItem(
                            productId = product.id!!,
                            quantity = 3
                        )
                    ),
                )
            )
        }?.toOrderResponse() ?: fail("result is expected")


        // when
        val response = restTemplate
            .exchange("/api/v1/orders/${orderCreated.id}", DELETE, null, respTypeRef<OrderResponse>())

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.NO_CONTENT)

        // and
        assertThrows<OrderNotFoundException> {
            transactionTemplate.execute {
                orderService.getOrderById(orderCreated.id!!)
            }
        }
    }

    @Test
    fun `should get invoice`() {

        // given
        val product = transactionTemplate.execute {
            productRepository.save(
                Product(
                    name = "test product",
                    price = BigDecimal.TEN.setScale(2),
                )
            )
        } ?: fail("result is expected")

        // and
        val order = transactionTemplate.execute {
            orderRepository.save(
                Order(
                    orderItems = setOf(
                        OrderItem(
                            productId = product.id!!,
                            quantity = 3
                        )
                    ),
                )
            )
        }?.toOrderResponse() ?: fail("result is expected")


        // when
        val response = restTemplate
            .exchange("/api/v1/orders/${order.id}/invoice", GET, null, respTypeRef<OrderInvoice>())

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        // and
        val body = response.body ?: fail("body may not be null")
        assertThat(body.orderId).isEqualTo((order.id))
        assertThat(body.total).isEqualTo(BigDecimal("30.00")) // quantity(3) * price(10.00)
    }
}
