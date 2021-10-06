package sigma.software.leovegas.drugstore.order.restdoc

import com.fasterxml.jackson.databind.ObjectMapper
import io.restassured.RestAssured
import io.restassured.parsing.Parser
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.MediaType
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.infrastructure.extensions.get
import sigma.software.leovegas.drugstore.order.OrderProperties
import sigma.software.leovegas.drugstore.order.OrderRepository
import sigma.software.leovegas.drugstore.order.OrderService
import sigma.software.leovegas.drugstore.order.api.CreateOrderEvent
import sigma.software.leovegas.drugstore.order.api.OrderItemDTO
import sigma.software.leovegas.drugstore.order.api.UpdateOrderEvent


@DisplayName("Update order REST API Doc test")
class RestApiDocUpdateOrderTest @Autowired constructor(
    val objectMapper: ObjectMapper,
    val transactionTemplate: TransactionTemplate,
    val orderProperties: OrderProperties,
    val orderRepository: OrderRepository,
    val orderService: OrderService,
    @LocalServerPort val port: Int,
) : RestApiDocumentationTest(orderProperties) {

    @Test
    fun `should update order`() {

        // setup
        transactionTemplate.execute { orderRepository.deleteAll() }

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
        }.get()

        // and
        val orderJson = objectMapper
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(
                UpdateOrderEvent(
                    orderNumber = "1",
                    listOf(
                        OrderItemDTO(
                            productNumber = "1",
                            quantity = 4
                        )
                    )
                )
            )
        RestAssured.registerParser("text/plain", Parser.TEXT);
        of("update-order")
            .pathParam("orderNumber", orderCreated.orderNumber)
            .`when`()
            .body(orderJson)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .put("http://${orderProperties.host}:$port/api/v1/orders/{orderNumber}")
            .then()
            .assertThat().statusCode(202)
            .assertThat().body(equalTo("Updated"))
    }
}
