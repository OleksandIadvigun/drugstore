package sigma.software.leovegas.drugstore.order

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.matching.ContainsPattern
import java.math.BigDecimal
import org.assertj.core.api.Assertions.fail
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.http.MediaType
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.product.api.ProductDetailsResponse

@DisplayName("Get orderDetails REST API Doc test")
@AutoConfigureWireMock(port = 8081)
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
                        ),
                        OrderItem(
                            productId = 2,
                            quantity = 2
                        )
                    )
                )
            )
        } ?: fail("result is expected")

        // and
        stubFor(
            get("/api/v1/products/details?ids=1&ids=2")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(
                    aResponse()
                        .withBody(
                            objectMapper
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(
                                    listOf(
                                        ProductDetailsResponse(
                                            id = 1,
                                            name = "test1",
                                            quantity = 3,
                                            price = BigDecimal.ONE
                                        ),
                                        ProductDetailsResponse(
                                            id = 2,
                                            name = "test2",
                                            quantity = 4,
                                            price = BigDecimal.TEN
                                        )
                                    )
                                )
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
            .assertThat().body("total", equalTo(21.0F)) // price multiply quantity
    }
}
