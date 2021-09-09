package sigma.software.leovegas.drugstore.accountancy.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.matching.ContainsPattern
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import sigma.software.leovegas.drugstore.accountancy.api.ItemDTO

@SpringBootApplication
internal class GetInvoiceByOrderIdFeignClientWireMockTestApp

@DisplayName("Get Invoice Details By Order Id Feign Client WireMock test")
@ContextConfiguration(classes = [GetInvoiceByOrderIdFeignClientWireMockTestApp::class])
class GetInvoiceByOrderIdFeignClientWireMockTest @Autowired constructor(
    val accountancyClient: AccountancyClient,
    val objectMapper: ObjectMapper
) : WireMockTest() {

    @Test
    fun `should get invoice details by order id`() {

        // given
        val responseExpected = listOf(
            ItemDTO(
                productId = 1L,
                quantity = 2
            )
        )

        // and
        stubFor(
            get("/api/v1/accountancy/invoice/details/order-id/1")
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
        val responseActual = accountancyClient.getInvoiceDetailsByOrderId(1L)

        // then
        assertThat(responseActual[0].productId).isEqualTo(1L)
        assertThat(responseActual[0].quantity).isEqualTo(2)
    }
}
