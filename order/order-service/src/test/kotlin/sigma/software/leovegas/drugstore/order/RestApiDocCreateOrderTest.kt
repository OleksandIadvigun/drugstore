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
import sigma.software.leovegas.drugstore.order.api.CreateOrderRequest
import sigma.software.leovegas.drugstore.order.api.OrderItemDTO

@DisplayName("Create order REST API Doc test")
class RestApiDocCreateOrderTest(
    @Autowired val objectMapper: ObjectMapper,
    @Autowired @LocalServerPort val port: Int,
) : RestApiDocumentationTest() {

    @Test
    fun `should create order`() {
        // given
        val orderJson = objectMapper
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(
                CreateOrderRequest(
                    listOf(
                        OrderItemDTO(
                            productId = 1L,
                            quantity = 3
                        )
                    )
                )
            )

        of("create-order").`when`()
            .body(orderJson)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .post("http://localhost:$port/api/v1/orders")
            .then()
            .assertThat().statusCode(201)
            .assertThat().body("orderStatus",equalTo("CREATED"))
            .assertThat().body("createdAt", not(emptyString()))
            .assertThat().body("updatedAt", not(emptyString()))
            .assertThat().body("orderItems[0].productId",equalTo(1))
            .assertThat().body("orderItems[0].quantity",equalTo(3))

    }
}