package sigma.software.leovegas.drugstore.order

import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.order.api.CreateOrderRequest
import sigma.software.leovegas.drugstore.order.api.OrderItemDTO

@DisplayName("Get orders REST API Doc test")
class RestApiDocGetOrdersTest(
    @Autowired @LocalServerPort val port: Int,
    @Autowired val transactionTemplate: TransactionTemplate,
    @Autowired val orderService: OrderService,
    @Autowired val orderRepository: OrderRepository,
) :RestApiDocumentationTest() {


    @Test
    fun `should get orders`() {

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
                            productId = 1L,
                            quantity = 3
                        )
                    )
                )
            )
        }

        if (orderCreated != null) {
            of("get-orders").`when`()
                .get("http://localhost:$port/api/v1/orders")
                .then()
                .assertThat().statusCode(200)
                .assertThat().body("size()", `is`(1))

        }

    }
}
