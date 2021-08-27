package sigma.software.leovegas.drugstore.order

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.matching.ContainsPattern
import java.math.BigDecimal
import org.assertj.core.api.Assertions.fail
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.MediaType
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.accountancy.api.PriceItemResponse
import sigma.software.leovegas.drugstore.product.api.ProductResponse

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

        // setup
        val wireMockServer8081 = WireMockServer(8081)
        val wireMockServer8084 = WireMockServer(8084)
        wireMockServer8081.start()
        wireMockServer8084.start()

        // and
        wireMockServer8081.stubFor(
            get("/api/v1/products-by-ids/?ids=1&ids=2")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(
                    aResponse()
                        .withBody(
                            objectMapper
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(
                                    listOf(
                                        ProductResponse(
                                            id = 1L,
                                            name = "test1",
                                        ),
                                        ProductResponse(
                                            id = 2L,
                                            name = "test2",
                                        )
                                    )
                                )
                        )
                )
        )

        // and
        wireMockServer8084.stubFor(
            get("/api/v1/accountancy/price-items-by-ids/ids=1,2&markup=true")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(
                    aResponse()
                        .withBody(
                            objectMapper
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(
                                    listOf(
                                        PriceItemResponse(
                                            id = 1,
                                            productId = 1,
                                            price = BigDecimal("20.00")
                                        ),
                                        PriceItemResponse(
                                            id = 2,
                                            productId = 2,
                                            price = BigDecimal("40.00")
                                        )
                                    )
                                )
                        )
                )
        )

        // and
        val order = transactionTemplate.execute {
            orderRepository.save(
                Order(
                    orderItems = setOf(
                        OrderItem(
                            priceItemId = 1L,
                            quantity = 1
                        ),
                        OrderItem(
                            priceItemId = 2L,
                            quantity = 2
                        )
                    )
                )
            )
        } ?: fail("result is expected")

        of("get-order-details").pathParam("id", order.id).`when`()
            .get("http://${orderProperties.host}:$port/api/v1/orders/{id}/details")
            .then()
            .assertThat().statusCode(200)
            .assertThat().body("orderItemDetails[0].name", equalTo("test1"))
            .assertThat().body("orderItemDetails[0].quantity", equalTo(1))
            .assertThat().body("orderItemDetails[0].priceItemId", equalTo(1))
            .assertThat().body("orderItemDetails[0].price", equalTo(20.0F))
            .assertThat().body("total", equalTo(100.0F)) // price multiply quantity

        // close server
        wireMockServer8084.stop()
        wireMockServer8081.stop()
    }
}
