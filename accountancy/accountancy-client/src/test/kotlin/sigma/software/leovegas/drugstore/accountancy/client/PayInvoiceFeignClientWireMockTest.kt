package sigma.software.leovegas.drugstore.accountancy.client

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
import sigma.software.leovegas.drugstore.accountancy.api.InvoiceResponse
import sigma.software.leovegas.drugstore.accountancy.api.InvoiceStatusDTO
import sigma.software.leovegas.drugstore.accountancy.api.ProductItemDTO

@SpringBootApplication
internal class PayInvoiceFeignClientWireMockTestApp

@DisplayName("Pay Invoice Feign Client WireMock test")
@ContextConfiguration(classes = [PayInvoiceFeignClientWireMockTestApp::class])
class PayInvoiceFeignClientWireMockTest @Autowired constructor(
    val accountancyClient: AccountancyClient,
    val objectMapper: ObjectMapper
) : WireMockTest() {

    @Test
    fun `should pay invoice`() {

        // given
        val responseExpected = InvoiceResponse(
            id = 1,
            orderId = 1,
            status = InvoiceStatusDTO.PAID,
            productItems = setOf(
                ProductItemDTO(
                    priceItemId = 1L,
                    name = "test",
                    price = BigDecimal("40.00"),
                    quantity = 3
                )
            ),
            total = BigDecimal("120.00"), // price * quantity
        )

        // and
        stubFor(
            put("/api/v1/accountancy/invoice/pay/1")
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
        val responseActual = accountancyClient.payInvoice(1)

        // then
        assertThat(responseActual.id).isEqualTo(1)
        assertThat(responseActual.orderId).isEqualTo(1)
        assertThat(responseActual.status).isEqualTo(InvoiceStatusDTO.PAID)
    }
}
