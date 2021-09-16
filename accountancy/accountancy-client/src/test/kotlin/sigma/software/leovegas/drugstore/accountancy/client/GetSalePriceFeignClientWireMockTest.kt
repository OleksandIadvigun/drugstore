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

@SpringBootApplication
internal class GetSalePriceFeignClientWireMockTestApp

@DisplayName("Get Sale Price Feign Client WireMock test")
@ContextConfiguration(classes = [GetSalePriceFeignClientWireMockTestApp::class])
class GetSalePriceFeignClientWireMockTest @Autowired constructor(
    val accountancyClient: AccountancyClient,
    val objectMapper: ObjectMapper
) : WireMockTest() {

    @Test
    fun `should sale price`() {

        // given
        val productsId = listOf("1", "2")

        // and
        val responseExpected = mapOf(
            Pair(productsId[0], BigDecimal("10.00")),
            Pair(productsId[1], BigDecimal("20.00"))
        )

        // and
        stubFor(
            get("/api/v1/accountancy/sale-price?productNumbers=1&productNumbers=2")
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
        val responseActual = accountancyClient.getSalePrice(productsId)

        // then
        assertThat(responseActual).hasSize(2)
        assertThat(responseActual.getValue(productsId[0])).isEqualTo(BigDecimal("10.00"))
        assertThat(responseActual.getValue(productsId[1])).isEqualTo(BigDecimal("20.00"))
    }
}
