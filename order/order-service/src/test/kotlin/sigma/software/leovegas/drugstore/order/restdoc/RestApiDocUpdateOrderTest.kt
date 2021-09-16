package sigma.software.leovegas.drugstore.order.restdoc

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
import sigma.software.leovegas.drugstore.infrastructure.extensions.get
import sigma.software.leovegas.drugstore.order.OrderProperties
import sigma.software.leovegas.drugstore.order.OrderRepository
import sigma.software.leovegas.drugstore.order.OrderService
import sigma.software.leovegas.drugstore.order.api.CreateOrderRequest
import sigma.software.leovegas.drugstore.order.api.OrderItemDTO
import sigma.software.leovegas.drugstore.order.api.UpdateOrderRequest

@DisplayName("Update order REST API Doc test")
class RestApiDocUpdateOrderTest @Autowired constructor(
    val objectMapper: ObjectMapper,
    @LocalServerPort val port: Int,
    val transactionTemplate: TransactionTemplate,
    val orderService: OrderService,
    val orderProperties: OrderProperties,
    val orderRepository: OrderRepository
) : RestApiDocumentationTest(orderProperties) {


    @Test
    fun `should update order`() {

        // setup
        transactionTemplate.execute { orderRepository.deleteAll() }

        // given
        val orderCreated = transactionTemplate.execute {
            orderService.createOrder(
                CreateOrderRequest(
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
                UpdateOrderRequest(
                    listOf(
                        OrderItemDTO(
                            productNumber = "1",
                            quantity = 4
                        )
                    )
                )
            )

            of("update-order")
                .pathParam("orderNumber", orderCreated.orderNumber)
                .`when`()
                .body(orderJson)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .put("http://${orderProperties.host}:$port/api/v1/orders/{orderNumber}")
                .then()
                .assertThat().statusCode(202)
                .assertThat().body("orderStatus", equalTo("UPDATED"))
                .assertThat().body("createdAt", not(emptyString()))
                .assertThat().body("updatedAt", not(emptyString()))
                .assertThat().body("orderItems[0].productNumber", equalTo("1"))
                .assertThat().body("orderItems[0].quantity", equalTo(4))
    }
}
