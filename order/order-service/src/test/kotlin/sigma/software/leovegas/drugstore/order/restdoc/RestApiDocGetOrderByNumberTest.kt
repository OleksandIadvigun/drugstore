package sigma.software.leovegas.drugstore.order.restdoc

import org.hamcrest.Matchers.emptyString
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.order.OrderProperties
import sigma.software.leovegas.drugstore.order.OrderService
import sigma.software.leovegas.drugstore.order.api.CreateOrderEvent
import sigma.software.leovegas.drugstore.order.api.OrderItemDTO

@DisplayName("Get order by id REST API Doc test")
class RestApiDocGetOrderByNumberTest @Autowired constructor(
    @LocalServerPort val port: Int,
    val transactionTemplate: TransactionTemplate,
    val orderService: OrderService,
    val orderProperties: OrderProperties
) : RestApiDocumentationTest(orderProperties) {

    @Test
    fun `should get order by orderNumber`() {

        // given
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
            of("get-order-by-number").pathParam("orderNumber", orderCreated.orderNumber).`when`()
                .get("http://${orderProperties.host}:$port/api/v1/orders/{orderNumber}")
                .then()
                .assertThat().statusCode(200)
                .assertThat().body("orderStatus", equalTo("CREATED"))
                .assertThat().body("createdAt", not(emptyString()))
                .assertThat().body("updatedAt", not(emptyString()))
                .assertThat().body("orderItems[0].productNumber", equalTo("1"))
                .assertThat().body("orderItems[0].quantity", equalTo(3))
        }
    }
}
