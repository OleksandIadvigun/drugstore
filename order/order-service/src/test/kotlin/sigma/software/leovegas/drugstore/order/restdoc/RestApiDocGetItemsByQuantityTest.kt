package sigma.software.leovegas.drugstore.order.restdoc

import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.infrastructure.RestApiDocumentationTest
import sigma.software.leovegas.drugstore.infrastructure.extensions.get
import sigma.software.leovegas.drugstore.order.Order
import sigma.software.leovegas.drugstore.order.OrderItem
import sigma.software.leovegas.drugstore.order.OrderItemRepository
import sigma.software.leovegas.drugstore.order.OrderProperties
import sigma.software.leovegas.drugstore.order.OrderRepository
import sigma.software.leovegas.drugstore.order.OrderStatus

@DisplayName("Get items sorted by quantity REST API Doc test")
class RestApiDocGetItemsByQuantityTest @Autowired constructor(
    val transactionTemplate: TransactionTemplate,
    val orderItemRepository: OrderItemRepository,
    val orderRepository: OrderRepository,
    val orderProperties: OrderProperties,
    @LocalServerPort val port: Int,
) : RestApiDocumentationTest(orderProperties) {

    @Disabled
    @Test
    fun `should get total buys of each product`() {

        // given
        transactionTemplate.execute { orderItemRepository.deleteAllInBatch() }
        transactionTemplate.execute { orderRepository.deleteAllInBatch() }

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
            .assertThat().body("productQuantityItemMap.size()", `is`(2))
    }
}
