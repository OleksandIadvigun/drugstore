package sigma.software.leovegas.drugstore.accountancy.client

import com.fasterxml.jackson.databind.ObjectMapper
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
import sigma.software.leovegas.drugstore.accountancy.api.ConfirmOrderResponse
import sigma.software.leovegas.drugstore.accountancy.api.CreateOutcomeInvoiceRequest
import sigma.software.leovegas.drugstore.accountancy.api.ItemDTO

@SpringBootApplication
internal class CreateOutcomeInvoiceFeignClientWireMockTestApp

@DisplayName("Create Outcome Invoice Feign Client WireMock test")
@ContextConfiguration(classes = [CreateOutcomeInvoiceFeignClientWireMockTestApp::class])
class CreateOutcomeInvoiceFeignClientWireMockTest @Autowired constructor(
    val accountancyClient: AccountancyClient,
    val objectMapper: ObjectMapper
) : WireMockTest() {

    @Test
    fun `should create outcome invoice`() {

        // given
        val request = CreateOutcomeInvoiceRequest(
            orderNumber = "1",
            productItems = listOf(
                ItemDTO(
                    productNumber = "1",
                    quantity = 5
                )
            )
        )

        // and
        val responseExpected = ConfirmOrderResponse(
            orderNumber = "1",
            amount = BigDecimal("15.00"), // price * quantity
        )

        // and
        stubFor(
            post("/api/v1/accountancy/invoice/outcome")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
                .withRequestBody(
                    EqualToPattern(
                        objectMapper
                            .writerWithDefaultPrettyPrinter()
                            .writeValueAsString(request)
                    )
                )
                .willReturn(
                    aResponse()
                        .withBody(
                            objectMapper
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(responseExpected)
                        )
                        .withStatus(HttpStatus.CREATED.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                )
        )

        // when
        val responseActual = accountancyClient.createOutcomeInvoice(request)

        // then
        assertThat(responseActual.orderNumber).isEqualTo("1")
        assertThat(responseActual.amount).isEqualTo(BigDecimal("15.00"))
    }
}
