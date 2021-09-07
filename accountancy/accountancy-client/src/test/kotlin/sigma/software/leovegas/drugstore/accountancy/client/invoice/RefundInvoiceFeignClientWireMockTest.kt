package sigma.software.leovegas.drugstore.accountancy.client.invoice

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.put
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.matching.ContainsPattern
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
import sigma.software.leovegas.drugstore.accountancy.client.AccountancyClient
import sigma.software.leovegas.drugstore.accountancy.client.WireMockTest

@SpringBootApplication
internal class RefundInvoiceFeignClientWireMockTestApp

@DisplayName("Refund Invoice By Id Feign Client WireMock test")
@ContextConfiguration(classes = [RefundInvoiceFeignClientWireMockTestApp::class])
class RefundInvoiceFeignClientWireMockTest @Autowired constructor(
    val accountancyClient: AccountancyClient,
    val objectMapper: ObjectMapper
) : WireMockTest() {

    @Test
    fun `should refund invoice`() {

        // given
        val responseExpected = ConfirmOrderResponse(
            orderId = 1,
            amount = BigDecimal("120.00"), // price * quantity
        )

        // and
        stubFor(
            put("/api/v1/accountancy/invoice/refund/1")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(
                    aResponse()
                        .withBody(
                            objectMapper
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(responseExpected)
                        )
                        .withStatus(HttpStatus.ACCEPTED.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                )
        )

        // when
        val responseActual = accountancyClient.refundInvoice(1)

        // then
        assertThat(responseActual.orderId).isEqualTo(responseExpected.orderId)
        assertThat(responseActual.amount).isEqualTo(responseExpected.amount)
    }
}
