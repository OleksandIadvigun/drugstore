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

    val product = transactionTemplate.execute {
        productRepository.save(
            Product(
                name = "test product",
                quantity = 5,
                price = BigDecimal.TEN.setScale(2),
            )
        )
    } ?: fail("result is expected")

    @Test
    fun `should create order`() {
        // given
        val httpEntity = HttpEntity(
            OrderRequest(
                listOf(
                    OrderDetailsRequest(1L, 3)
                )
            )
        )

        // when
        val response = restTemplate.exchange("/orders", POST, httpEntity, respTypeRef<OrderResponse>())

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)

        // and
        val body = response.body ?: fail("body may not be null")
        assertThat(body.id).isNotNull
        assertThat(body.orderDetailsList).hasSize(1)
    }

    @Test
    fun `should get order by id`() {

        //given
        val orderCreated = transactionTemplate.execute {
            orderRepository.save(
                Order(
                    orderDetailsList = listOf(
                        OrderDetails(
                            product = product,
                            quantity = 3
                        )
                    ),
                    total = BigDecimal(30.00).setScale(2) //price * orderDetails.quantity
                )
            )
        }?.toOrderResponse() ?: fail("result is expected")

        // when
        val response = restTemplate
            .exchange("/orders/${orderCreated.id}", GET, null, respTypeRef<OrderResponse>())

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        // and
        val body = response.body ?: fail("body may not be null")
        assertThat(body.id).isEqualTo(orderCreated.id)
        assertThat(body.total).isEqualTo(orderCreated.total)
        assertThat(body.orderDetailsList).isEqualTo(orderCreated.orderDetailsList)

    }

    @Test
    fun `should get orders`() {


        // when
        val response = restTemplate
            .exchange("/orders", GET, null, respTypeRef<List<OrderResponse>>())

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        // and
        val body = response.body ?: fail("body may not be null")
        assertThat(body).isNotNull
    }

    @Test
    fun `should update order`() {

        //given
        val orderCreated = transactionTemplate.execute {
            orderRepository.save(
                Order(
                    orderDetailsList = listOf(
                        OrderDetails(
                            product = product,
                            quantity = 3
                        )
                    ),
                    total = BigDecimal(30.00).setScale(2) //price * orderDetails.quantity
                )
            )
        }?.toOrderResponse() ?: fail("result is expected")

        // and
        val httpEntity = HttpEntity(
            OrderRequest(
                listOf(
                    OrderDetailsRequest(product.id, 5)
                )
            )
        )

        // when
        val response = restTemplate
            .exchange("/orders/${orderCreated.id}", PUT, httpEntity, respTypeRef<OrderResponse>())

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.ACCEPTED)

        // and
        val body = response.body ?: fail("body may not be null")
        assertThat(body).isNotNull
        assertThat(body.id).isEqualTo(orderCreated.id)
        assertThat(body.orderDetailsList[0].quantity).isEqualTo(httpEntity.body?.orderDetailsList?.get(0)?.quantity)
        assertThat(body.total).isEqualTo(BigDecimal("50.00"))
    }

    @Test
    fun `should delete order`() {

        //given
        val orderCreated = transactionTemplate.execute {
            orderRepository.save(
                Order(
                    orderDetailsList = listOf(
                        OrderDetails(
                            product = product,
                            quantity = 3
                        )
                    ),
                    total = BigDecimal(30.00).setScale(2) //price * orderDetails.quantity
                )
            )
        }?.toOrderResponse() ?: fail("result is expected")


        // when
        val response = restTemplate
            .exchange("/orders/${orderCreated.id}", DELETE, null, respTypeRef<OrderResponse>())

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.NO_CONTENT)

        // and
        assertThrows<OrderNotFoundException> {
            transactionTemplate.execute {
                orderService.getOrderById(orderCreated.id)
            }
        }
    }
}
