package sigma.software.leovegas.drugstore.order

import org.hamcrest.Matchers.emptyString
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.order.api.CreateOrderRequest
import sigma.software.leovegas.drugstore.order.api.OrderItemDTO

@DisplayName("Get order by id REST API Doc test")
class RestApiDocGetOrderByIdTest(
    @Autowired @LocalServerPort val port: Int,
    @Autowired val transactionTemplate: TransactionTemplate,
    @Autowired val orderService: OrderService,
) :RestApiDocumentationTest() {


    @Test
    fun `should get order by id`() {

        // given
        val orderCreated = transactionTemplate.execute {
            orderService.createOrder(
                CreateOrderRequest(
                    listOf(
                        OrderItemDTO(
                            productId = 1L,
                            quantity = 3
                        )
                    )
                )
            )
        }
        println(orderCreated)

        if (orderCreated != null) {
            of("get-order-by-id").pathParam("id",orderCreated.id).`when`()
                .get("http://localhost:$port/api/v1/orders/{id}")
                .then()
                .assertThat().statusCode(200)
                .assertThat().body("orderStatus", equalTo("CREATED"))
                .assertThat().body("createdAt", not(emptyString()))
                .assertThat().body("updatedAt", not(emptyString()))
                .assertThat().body("orderItems[0].productId", equalTo(1))
                .assertThat().body("orderItems[0].quantity", equalTo(3))
        }

    }
}