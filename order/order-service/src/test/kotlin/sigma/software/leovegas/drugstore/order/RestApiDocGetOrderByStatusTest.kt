package sigma.software.leovegas.drugstore.order

import org.hamcrest.Matchers.emptyString
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.order.api.CreateOrderRequest
import sigma.software.leovegas.drugstore.order.api.OrderItemDTO

@DisplayName("Get order by status REST API Doc test")
class RestApiDocGetOrderByStatusTest @Autowired constructor(
    @LocalServerPort val port: Int,
    val transactionTemplate: TransactionTemplate,
    val orderService: OrderService,
    val orderProperties: OrderProperties,
    val orderRepository: OrderRepository
) : RestApiDocumentationTest() {

    @Test
    fun `should get order by status`() {

        // given
        transactionTemplate.execute {
            orderRepository.deleteAll()
        }

        // and
        val orderCreated = transactionTemplate.execute {
            orderService.createOrder(
                CreateOrderRequest(
                    listOf(
                        OrderItemDTO(
                            priceItemId = 1L,
                            quantity = 3
                        )
                    )
                )
            )
        } ?: fail("result is expected")

        of("get-order-by-status").pathParam("status", orderCreated.orderStatus).`when`()
            .get("http://${orderProperties.host}:$port/api/v1/orders/status/{status}")
            .then()
            .assertThat().statusCode(200)
            .assertThat().body("[0].orderStatus", equalTo("CREATED"))
            .assertThat().body("[0].createdAt", not(emptyString()))
            .assertThat().body("[0].updatedAt", not(emptyString()))
            .assertThat().body("[0].orderItems[0].priceItemId", equalTo(1))
            .assertThat().body("[0].orderItems[0].quantity", equalTo(3))
    }
}
