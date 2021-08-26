package sigma.software.leovegas.drugstore.store

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.put
import com.github.tomakehurst.wiremock.matching.ContainsPattern
import com.github.tomakehurst.wiremock.matching.EqualToPattern
import io.restassured.http.ContentType
import java.math.BigDecimal
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.accountancy.api.InvoiceResponse
import sigma.software.leovegas.drugstore.accountancy.api.InvoiceStatusDTO
import sigma.software.leovegas.drugstore.order.api.OrderResponse
import sigma.software.leovegas.drugstore.order.api.OrderStatusDTO

@DisplayName("Delivery goods REST API Doc test")
class RestApiDocDeliveryGoodsTest @Autowired constructor(
    val objectMapper: ObjectMapper,
    @LocalServerPort val port: Int,
    val storeProperties: StoreProperties,
    val storeRepository: StoreRepository,
    val transactionTemplate: TransactionTemplate
) : RestApiDocumentationTest() {

    @Test
    fun `should deliver goods`() {

        // setup
        val wireMockServer8084 = WireMockServer(8084)
        val wireMockServer8082 = WireMockServer(8082)
        wireMockServer8084.start()
        wireMockServer8082.start()

        // given
        wireMockServer8084.stubFor(
            get("/api/v1/accountancy/invoice/order-id/1")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(
                    aResponse()
                        .withBody(
                            objectMapper
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(
                                    InvoiceResponse(
                                        id = 1,
                                        orderId = 1,
                                        total = BigDecimal("90.00"),
                                        status = InvoiceStatusDTO.PAID
                                    )
                                )
                        )
                )
        )

        // and
        wireMockServer8082.stubFor(
            put("/api/v1/orders/change-status/1")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
                .withRequestBody(
                    EqualToPattern(
                        objectMapper
                            .writerWithDefaultPrettyPrinter()
                            .writeValueAsString(OrderStatusDTO.DELIVERED)
                    )
                )
                .willReturn(
                    aResponse()
                        .withBody(
                            objectMapper
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(OrderResponse(orderStatus = OrderStatusDTO.DELIVERED))
                        )
                        .withStatus(HttpStatus.OK.value())
                )
        )

        // and
        val storeJson = objectMapper
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(
                "DELIVERED"
                )

        of("delivery-goods").`when`()
            .pathParam("invoice-id", 1)
            .put("http://${storeProperties.host}:$port/api/v1/store/delivery/{invoice-id}")
            .then()
            .contentType(ContentType.TEXT)
            .assertThat().statusCode(202)
            .extract()

        wireMockServer8082.stop()
        wireMockServer8084.stop()
    }
}
