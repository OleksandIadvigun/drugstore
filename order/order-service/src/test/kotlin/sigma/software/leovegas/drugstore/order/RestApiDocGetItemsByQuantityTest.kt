package sigma.software.leovegas.drugstore.order

import org.assertj.core.api.Assertions.fail
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.transaction.support.TransactionTemplate

@DisplayName("Get items sorted by quantity REST API Doc test")
class RestApiDocGetItemsByQuantityTest @Autowired constructor(
    @LocalServerPort val port: Int,
    val transactionTemplate: TransactionTemplate,
    val orderService: OrderService,
    val orderRepository: OrderRepository,
    val orderProperties: OrderProperties
) : RestApiDocumentationTest(orderProperties) {

    @Test
    fun `should get total buys of each product`() {

        // given
        transactionTemplate.execute {
            orderRepository.deleteAll()
        }

        // and
        transactionTemplate.execute {
            orderRepository.saveAll(
                listOf(
                    Order(
                        orderStatus = OrderStatus.PAID,
                        orderItems = setOf(
                            OrderItem(
                                priceItemId = 1,
                                quantity = 3
                            )
                        ),
                    ),
                    Order(
                        orderStatus = OrderStatus.PAID,
                        orderItems = setOf(
                            OrderItem(
                                priceItemId = 2,
                                quantity = 5
                            )
                        ),
                    )
                )
            )
        } ?: fail("result is expected")

        of("get-sorted-items").`when`()
            .get("http://${orderProperties.host}:$port/api/v1/orders/total-buys")
            .then()
            .assertThat().statusCode(200)
            .assertThat().body("size()", `is`(2))
    }
}
