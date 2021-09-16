package sigma.software.leovegas.drugstore.order.restdoc

import com.fasterxml.jackson.databind.ObjectMapper
import io.restassured.RestAssured
import io.restassured.parsing.Parser
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.MediaType
import sigma.software.leovegas.drugstore.order.OrderProperties
import sigma.software.leovegas.drugstore.order.api.CreateOrderEvent
import sigma.software.leovegas.drugstore.order.api.OrderItemDTO


@DisplayName("Create order REST API Doc test")
class RestApiDocCreateOrderTest @Autowired constructor(
    val objectMapper: ObjectMapper,
    @LocalServerPort val port: Int,
    val orderProperties: OrderProperties
) : RestApiDocumentationTest(orderProperties) {


    @Test
    fun `should create order`() {

        // given
        val orderJson = objectMapper
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(
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
        RestAssured.registerParser("text/plain", Parser.TEXT);
        of("create-order").`when`()
            .body(orderJson)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .post("http://${orderProperties.host}:$port/api/v1/orders")
            .then()
            .assertThat().statusCode(201)
            .assertThat().body(not("undefined"))

    }
}
