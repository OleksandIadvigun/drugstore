package sigma.software.leovegas.drugstore.accountancy.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.matching.ContainsPattern
import com.github.tomakehurst.wiremock.matching.EqualToPattern
import java.math.BigDecimal
import java.time.LocalDateTime
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import sigma.software.leovegas.drugstore.accountancy.api.InvoiceRequest
import sigma.software.leovegas.drugstore.accountancy.api.InvoiceResponse
import sigma.software.leovegas.drugstore.accountancy.api.InvoiceStatusDTO
import sigma.software.leovegas.drugstore.accountancy.api.ProductItemDTO

@SpringBootApplication
internal class CreateInvoiceFeignClientWireMockTestApp

@DisplayName("Create Invoice Feign Client WireMock test")
@ContextConfiguration(classes = [CreateProductFeignClientWireMockTestApp::class])
class CreateInvoiceFeignClientWireMockTest @Autowired constructor(
    val accountancyClient: AccountancyClient,
    val objectMapper: ObjectMapper
) : WireMockTest() {

    @Test
    fun `should create invoice`() {

        // given
        val request = InvoiceRequest(
            orderId = 1L
        )

        // and
        val responseExpected = InvoiceResponse(
            id = 1L,
            orderId = request.orderId,
            status = InvoiceStatusDTO.CREATED,
            productItems = setOf(
                ProductItemDTO(
                    priceItemId = 1L,
                    name = "test",
                    price = BigDecimal("40.00"),
                    quantity = 3
                )
            ),
            total = BigDecimal("120.00"), // price * quantity
            expiredAt = LocalDateTime.now().plusDays(3)
        )

        // and
        stubFor(
            post("/api/v1/accountancy/invoice")
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
        val responseActual = accountancyClient.createInvoice(request)

        // then
        assertThat(responseActual.id).isEqualTo(1L)
        assertThat(responseActual.orderId).isEqualTo(request.orderId)
        assertThat(responseActual.status).isEqualTo(InvoiceStatusDTO.CREATED)
        assertThat(responseActual.expiredAt).isBefore(LocalDateTime.now().plusDays(4))
        assertThat(responseActual.total).isEqualTo(BigDecimal("120.00"))
    }
}