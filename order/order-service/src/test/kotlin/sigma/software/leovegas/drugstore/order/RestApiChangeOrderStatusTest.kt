package sigma.software.leovegas.drugstore.order

import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.Matchers.emptyString
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.MediaType
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.order.api.CreateOrderRequest
import sigma.software.leovegas.drugstore.order.api.OrderItemDTO

@DisplayName("Change order status REST API Doc test")
class RestApiChangeOrderStatusTest @Autowired constructor(
    val objectMapper: ObjectMapper,
    @LocalServerPort val port: Int,
    val transactionTemplate: TransactionTemplate,
    val orderService: OrderService,
    val orderProperties: OrderProperties,
    val orderRepository: OrderRepository
) : RestApiDocumentationTest(orderProperties) {

    @Test
    fun `should update order`() {

        // given
        transactionTemplate.execute {
            orderRepository.deleteAll()
        }

        // given
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
        }

        // and
        val orderJson = objectMapper
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(
                OrderStatus.BOOKED
            )

        if (orderCreated != null) {
            of("update-order")
                .pathParam("id", orderCreated.id)
                .`when`()
                .body(orderJson)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .put("http://${orderProperties.host}:$port/api/v1/orders/change-status/{id}")
                .then()
                .assertThat().statusCode(202)
                .assertThat().body("orderStatus", equalTo("BOOKED"))
                .assertThat().body("createdAt", not(emptyString()))
                .assertThat().body("updatedAt", not(emptyString()))
                .assertThat().body("orderItems[0].priceItemId", equalTo(1))
                .assertThat().body("orderItems[0].quantity", equalTo(3))
        }
    }
}
