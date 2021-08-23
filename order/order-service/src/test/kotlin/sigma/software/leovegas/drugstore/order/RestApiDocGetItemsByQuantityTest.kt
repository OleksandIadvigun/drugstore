package sigma.software.leovegas.drugstore.order

import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.order.api.CreateOrderRequest
import sigma.software.leovegas.drugstore.order.api.OrderItemDTO

@DisplayName("Get items sorted by quantity REST API Doc test")
class RestApiDocGetItemsByQuantityTest @Autowired constructor(
    @LocalServerPort val port: Int,
    val transactionTemplate: TransactionTemplate,
    val orderService: OrderService,
    val orderRepository: OrderRepository,
    val orderProperties: OrderProperties
) : RestApiDocumentationTest() {

    @Test
    fun `should get total buys of each product`() {

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
                            quantity = 1
                        ),
                        OrderItemDTO(
                            priceItemId = 2L,
                            quantity = 5
                        ),
                        OrderItemDTO(
                            priceItemId = 3L,
                            quantity = 3
                        )
                    )
                )
            )
        }

        if (orderCreated != null) {
            of("get-sorted-items").`when`()
                .get("http://${orderProperties.host}:$port/api/v1/orders/total-buys")
                .then()
                .assertThat().statusCode(200)
                .assertThat().body("size()", `is`(3))
        }
    }
}
