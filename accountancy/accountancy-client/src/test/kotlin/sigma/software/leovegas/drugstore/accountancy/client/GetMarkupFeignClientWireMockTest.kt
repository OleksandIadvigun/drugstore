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
import sigma.software.leovegas.drugstore.accountancy.api.MarkupUpdateRequest
import sigma.software.leovegas.drugstore.accountancy.api.MarkupUpdateResponse

@SpringBootApplication
internal class GetMarkupFeignClientWireMockTestApp

@DisplayName("Update markup Feign Client WireMock test")
@ContextConfiguration(classes = [GetMarkupFeignClientWireMockTestApp::class])
class GetMarkupFeignClientWireMockTest @Autowired constructor(
    val accountancyClient: AccountancyClient,
    val objectMapper: ObjectMapper
) : WireMockTest() {

    @Test
    fun `should get markups`() {

        // given
        val request = listOf(
            MarkupUpdateRequest(
                priceItemId = 1L,
                markup = BigDecimal("0.20")
            ),
            MarkupUpdateRequest(
                priceItemId = 2L,
                markup = BigDecimal("0.30")
            )
        )

        //and
        val responseExpected = listOf(
            MarkupUpdateResponse(
                priceItemId = 1L,
                price = BigDecimal("12.00"),
                markup = request[0].markup
            ),
            MarkupUpdateResponse(
                priceItemId = 2L,
                price = BigDecimal("22.00"),
                markup = request[1].markup
            )
        )

        //and
        stubFor(
            get("/api/v1/accountancy/price-item/markup?ids=1&ids=2")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
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
        val responseActual = accountancyClient.getMarkups(listOf(1L,2L))

        //  then
        assertThat(responseActual[0].priceItemId).isEqualTo(1L)
        assertThat(responseActual[0].markup).isEqualTo(request[0].markup)
        assertThat(responseActual[1].markup).isEqualTo(request[1].markup)
    }
}
