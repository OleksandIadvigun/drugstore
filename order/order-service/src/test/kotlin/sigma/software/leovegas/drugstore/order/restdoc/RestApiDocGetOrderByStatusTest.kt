package sigma.software.leovegas.drugstore.order.restdoc

import org.hamcrest.Matchers.emptyString
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.infrastructure.RestApiDocumentationTest
import sigma.software.leovegas.drugstore.infrastructure.extensions.get
import sigma.software.leovegas.drugstore.order.OrderItemRepository
import sigma.software.leovegas.drugstore.order.OrderProperties
import sigma.software.leovegas.drugstore.order.OrderRepository
import sigma.software.leovegas.drugstore.order.OrderService
import sigma.software.leovegas.drugstore.order.api.CreateOrderEvent
import sigma.software.leovegas.drugstore.order.api.OrderItemDTO

@DisplayName("Get order by status REST API Doc test")
class RestApiDocGetOrderByStatusTest @Autowired constructor(
    val transactionTemplate: TransactionTemplate,
    val orderItemRepository: OrderItemRepository,
    val orderRepository: OrderRepository,
    val orderProperties: OrderProperties,
    val orderService: OrderService,
    @LocalServerPort val port: Int,
) : RestApiDocumentationTest(orderProperties) {

    @Test
    fun `should get order by status`() {

        // given
        transactionTemplate.execute { orderItemRepository.deleteAllInBatch() }
        transactionTemplate.execute { orderRepository.deleteAllInBatch() }

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
        }.get()

        of("get-order-by-status").pathParam("status", orderCreated.orderStatus).`when`()
            .get("http://${orderProperties.host}:$port/api/v1/orders/status/{status}")
            .then()
            .assertThat().statusCode(200)
            .assertThat().body("[0].orderStatus", equalTo("CREATED"))
            .assertThat().body("[0].createdAt", not(emptyString()))
            .assertThat().body("[0].updatedAt", not(emptyString()))
            .assertThat().body("[0].orderItems[0].productNumber", equalTo("1"))
            .assertThat().body("[0].orderItems[0].quantity", equalTo(3))
    }
}
