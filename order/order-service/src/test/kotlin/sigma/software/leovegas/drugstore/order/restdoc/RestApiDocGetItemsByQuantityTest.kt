package sigma.software.leovegas.drugstore.order.restdoc

import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.infrastructure.extensions.get
import sigma.software.leovegas.drugstore.order.Order
import sigma.software.leovegas.drugstore.order.OrderItem
import sigma.software.leovegas.drugstore.order.OrderProperties
import sigma.software.leovegas.drugstore.order.OrderRepository
import sigma.software.leovegas.drugstore.order.OrderStatus

@DisplayName("Get items sorted by quantity REST API Doc test")
class RestApiDocGetItemsByQuantityTest @Autowired constructor(
    @LocalServerPort val port: Int,
    val transactionTemplate: TransactionTemplate,
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
                        orderNumber = "1",
                        orderStatus = OrderStatus.CONFIRMED,
                        orderItems = setOf(
                            OrderItem(
                                productNumber = "1",
                                quantity = 3
                            )
                        ),
                    ),
                    Order(
                        orderNumber = "2",
                        orderStatus = OrderStatus.CONFIRMED,
                        orderItems = setOf(
                            OrderItem(
                                productNumber = "2",
                                quantity = 5
                            )
                        ),
                    )
                )
            )
        }.get()

        of("get-sorted-items").`when`()
            .get("http://${orderProperties.host}:$port/api/v1/orders/total-buys")
            .then()
            .assertThat().statusCode(200)
            .assertThat().body("size()", `is`(2))
    }
}
