package sigma.software.leovegas.drugstore.accountancy.client.priceitem

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
import sigma.software.leovegas.drugstore.accountancy.client.AccountancyClient
import sigma.software.leovegas.drugstore.accountancy.client.WireMockTest

@SpringBootApplication
internal class GetProductsPriceByProductIdsFeignClientWireMockApp

@DisplayName("Get Products Price By Products Ids Feign Client WireMock test")
@ContextConfiguration(classes = [GetProductsPriceByProductIdsFeignClientWireMockApp::class])
class GetProductsPriceByProductIdsFeignClientWireMockTest @Autowired constructor(
    val accountancyClient: AccountancyClient,
    val objectMapper: ObjectMapper
) : WireMockTest() {

    @Test
    fun `should get products price by products ids`() {

        // given
        val responseExpected = listOf(
            PriceItemResponse(
                productId = 1L,
                price = BigDecimal("20.00")
            ),
            PriceItemResponse(
                productId = 2L,
                price = BigDecimal("40.00")
            )
        )

        //and
        stubFor(
            get("/api/v1/accountancy/price-by-product-ids?ids=1&ids=2&markup=true")
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
        val responseActual = accountancyClient.getProductsPriceByProductIds(listOf(1L, 2L))

        //  then
        assertThat(responseActual.size).isEqualTo(2)
        assertThat(responseActual[0].price).isEqualTo(BigDecimal("20.00"))
        assertThat(responseActual[1].price).isEqualTo(BigDecimal("40.00"))
    }
}
