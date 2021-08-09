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
import sigma.software.leovegas.drugstore.order.api.UpdateOrderRequest

@DisplayName("Update order REST API Doc test")
class RestApiDocUpdateOrderTest(
    @Autowired val objectMapper: ObjectMapper,
    @Autowired @LocalServerPort val port: Int,
    @Autowired val transactionTemplate: TransactionTemplate,
    @Autowired val orderService: OrderService,
) : RestApiDocumentationTest() {


    @Test
    fun `should update order`() {

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

        // and
        val orderJson = objectMapper
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(
                UpdateOrderRequest(
                    listOf(
                        OrderItemDTO(
                            productId = 1L,
                            quantity = 4
                        )
                    )
                )
            )

        if (orderCreated != null) {
            of("update-order")
                .pathParam("id", orderCreated.id)
                .`when`()
                .body(orderJson)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .put("http://localhost:$port/api/v1/orders/{id}")
                .then()
                .assertThat().statusCode(202)
                .assertThat().body("orderStatus", equalTo("UPDATED"))
                .assertThat().body("createdAt", not(emptyString()))
                .assertThat().body("updatedAt", not(emptyString()))
                .assertThat().body("orderItems[0].productId", equalTo(1))
                .assertThat().body("orderItems[0].quantity", equalTo(4))
        }

    }
}