package sigma.software.leovegas.drugstore.order.restdoc

import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.order.OrderProperties
import sigma.software.leovegas.drugstore.order.OrderRepository
import sigma.software.leovegas.drugstore.order.OrderService
import sigma.software.leovegas.drugstore.order.api.CreateOrderEvent
import sigma.software.leovegas.drugstore.order.api.OrderItemDTO

@DisplayName("Get orders REST API Doc test")
class RestApiDocGetOrdersTest @Autowired constructor(
    val transactionTemplate: TransactionTemplate,
    val orderProperties: OrderProperties,
    val orderRepository: OrderRepository,
    val orderService: OrderService,
    @LocalServerPort val port: Int,
) : RestApiDocumentationTest(orderProperties) {


    @Test
    fun `should get orders`() {

        // given
        transactionTemplate.execute {
            orderRepository.deleteAll()
        }

        // and
        val orderCreated = transactionTemplate.execute {
            orderService.createOrder(
                CreateOrderEvent(
                    orderItems =
                    listOf(
                        OrderItemDTO(
                            productNumber = "1",
                            quantity = 3
                        )
                    )
                )
            )
        }

        if (orderCreated != null) {
            of("get-orders").`when`()
                .get("http://${orderProperties.host}:$port/api/v1/orders")
                .then()
                .assertThat().statusCode(200)
                .assertThat().body("size()", `is`(1))

        }
    }
}
