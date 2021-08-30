package sigma.software.leovegas.drugstore.accountancy.client.priceitem

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.put
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
import sigma.software.leovegas.drugstore.accountancy.api.MarkupUpdateRequest
import sigma.software.leovegas.drugstore.accountancy.api.MarkupUpdateResponse
import sigma.software.leovegas.drugstore.accountancy.client.AccountancyClient
import sigma.software.leovegas.drugstore.accountancy.client.WireMockTest

@SpringBootApplication
internal class UpdateMarkupFeignClientWireMockTestApp

@DisplayName("Update markup Feign Client WireMock test")
@ContextConfiguration(classes = [UpdateMarkupFeignClientWireMockTestApp::class])
class UpdateMarkupFeignClientWireMockTest @Autowired constructor(
    val accountancyClient: AccountancyClient,
    val objectMapper: ObjectMapper
) : WireMockTest() {

    @Test
    fun `should update markup`() {

        // given
        val request = listOf(
            MarkupUpdateRequest(
                priceItemId = 1L,
                markup = BigDecimal("0.20")
            )
        )

        //and
        val responseExpected = listOf(
            MarkupUpdateResponse(
                priceItemId = 1L,
                price = BigDecimal("12.00"),
                markup = request[0].markup
            )
        )

        //and
        stubFor(
            put("/api/v1/accountancy/price-item/markup")
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
        val responseActual = accountancyClient.updateMarkup(request)

        //  then
        assertThat(responseActual[0].priceItemId).isEqualTo(1L)
        assertThat(responseActual[0].markup).isEqualTo(request[0].markup)
    }
}
