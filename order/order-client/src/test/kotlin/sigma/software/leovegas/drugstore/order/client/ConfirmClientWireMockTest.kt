package sigma.software.leovegas.drugstore.order.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.matching.ContainsPattern
import com.github.tomakehurst.wiremock.matching.EqualToPattern
import java.math.BigDecimal
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import sigma.software.leovegas.drugstore.accountancy.api.InvoiceResponse
import sigma.software.leovegas.drugstore.accountancy.api.ProductItemDTO

@SpringBootApplication
internal class ConfirmClientWireMockTestApp

@DisplayName("Confirm order Feign Client WireMock test")
@ContextConfiguration(classes = [ConfirmClientWireMockTestApp::class])
class ConfirmClientWireMockTest @Autowired constructor(
    val orderClient: OrderClient,
    val objectMapper: ObjectMapper,
) : WireMockTest() {

    @Test
    fun `should confirm order`() {

        // given
        val wireMockProductAccountancy: WireMockServer = WireMockServer(8084)
        wireMockProductAccountancy.start()

        // and
        val responseExpected =
            InvoiceResponse(
                id = 1,
                orderId = 1L,
                productItems = setOf(
                    ProductItemDTO(
                        name = "aspirin",
                        price = BigDecimal("30.00"),
                        quantity = 3
                    )
                ),
                total = BigDecimal("90.00"),
            )

        // and
        wireMockProductAccountancy.stubFor(
            post("/api/v1/accountancy/invoice/outcome")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
                .withRequestBody(
                    EqualToPattern(
                        objectMapper
                            .writerWithDefaultPrettyPrinter()
                            .writeValueAsString(1L)
                    )
                )
                .willReturn(
                    aResponse()
                        .withBody(
                            objectMapper
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(responseExpected)
                        )
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                )
        )

        // and
        stubFor(
            post("/api/v1/orders/confirm")
                .withRequestBody(
                    EqualToPattern(
                        objectMapper
                            .writerWithDefaultPrettyPrinter()
                            .writeValueAsString(1L)
                    )
                )
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(
                    aResponse()
                        .withBody(
                            objectMapper
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(responseExpected)
                        )
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                )
        )

        // when
        val responseActual = orderClient.confirmOrder(1L)

        //  then
        assertThat(responseActual.productItems.size).isEqualTo(1L)
        assertThat(responseActual.productItems.iterator().next().name).isEqualTo("aspirin")
        assertThat(responseActual.productItems.iterator().next().quantity).isEqualTo(3)

        // and
        wireMockProductAccountancy.stop()
    }
}
