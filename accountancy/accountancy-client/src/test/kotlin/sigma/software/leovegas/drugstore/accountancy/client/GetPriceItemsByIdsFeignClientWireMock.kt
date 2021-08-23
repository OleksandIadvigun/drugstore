package sigma.software.leovegas.drugstore.accountancy.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
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
import sigma.software.leovegas.drugstore.accountancy.api.PriceItemResponse

@SpringBootApplication
internal class GetPriceItemsByIdsFeignClientWireMockApp

@DisplayName("Get Price Items By Ids Feign Client WireMock test")
@ContextConfiguration(classes = [GetPriceItemsByIdsFeignClientWireMockApp::class])
class GetProductsPriceByIdsFeignClientWireMock @Autowired constructor(
    val accountancyClient: AccountancyClient,
    val objectMapper: ObjectMapper
) : WireMockTest() {

    @Test
    fun `should get price items by ids`() {

        // given
        val responseExpected = listOf(
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

        //and
        stubFor(
            get("/api/v1/accountancy/price-items-by-ids/ids=1,2")
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
        val responseActual = accountancyClient.getPriceItemsByIds(listOf(1L, 2L))

        //  then
        assertThat(responseActual.size).isEqualTo(2)
        assertThat(responseActual[0].price).isEqualTo(BigDecimal("20.00"))
        assertThat(responseActual[0].id).isEqualTo(1)
        assertThat(responseActual[1].price).isEqualTo(BigDecimal("40.00"))
        assertThat(responseActual[1].id).isEqualTo(2)
    }
}
