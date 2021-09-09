package sigma.software.leovegas.drugstore.order.restdoc

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.matching.ContainsPattern
import java.math.BigDecimal
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.MediaType
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.infrastructure.extensions.get
import sigma.software.leovegas.drugstore.order.Order
import sigma.software.leovegas.drugstore.order.OrderItem
import sigma.software.leovegas.drugstore.order.OrderProperties
import sigma.software.leovegas.drugstore.order.OrderRepository
import sigma.software.leovegas.drugstore.order.OrderStatus
import sigma.software.leovegas.drugstore.product.api.ProductDetailsResponse

@DisplayName("Get orderDetails REST API Doc test")
class RestApiDocGetOrderDetailsTest @Autowired constructor(
    @LocalServerPort val port: Int,
    val transactionTemplate: TransactionTemplate,
    val orderRepository: OrderRepository,
    val objectMapper: ObjectMapper,
    val orderProperties: OrderProperties
) : RestApiDocumentationTest(orderProperties) {

    @Test
    fun `should get orderDetails`() {

        // given
        val order = transactionTemplate.execute {
            orderRepository.save(
                Order(
                    orderStatus = OrderStatus.CREATED,
                    orderItems = setOf(
                        OrderItem(
                            productId = 1,
                            quantity = 1
                        )
                    )
                )
            )
        }.get()

        // and
        val response = listOf(
            ProductDetailsResponse(
                id = 1,
                name = "test1",
                quantity = 3,
                price = BigDecimal.ONE
            )
        )

        // and
        stubFor(
            WireMock.get("/api/v1/products/details?ids=1")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(
                    aResponse()
                        .withBody(
                            objectMapper
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(response)
                        )
                )
        )


        of("get-order-details")
            .pathParam("id", order.id).`when`()
            .get("http://${orderProperties.host}:$port/api/v1/orders/{id}/details")
            .then()
            .assertThat().statusCode(200)
            .assertThat().body("orderItemDetails[0].name", equalTo("test1"))
            .assertThat().body("orderItemDetails[0].quantity", equalTo(1))
            .assertThat().body("orderItemDetails[0].price", equalTo(1))
            .assertThat().body("total", equalTo(1.0F)) // price multiply quantity
    }
}
