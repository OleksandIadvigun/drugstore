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
internal class GetProductsPriceFeignClientWireMockTestApp

@DisplayName("Get Products Price Feign Client WireMock test")
@ContextConfiguration(classes = [CreateProductFeignClientWireMockTestApp::class])
class GetProductsPriceFeignClientWireMockTest @Autowired constructor(
    val accountancyClient: AccountancyClient,
    val objectMapper: ObjectMapper
) : WireMockTest() {

    @Test
    fun `should get products price`() {

        // given
        val responseExpected = mapOf(
            1L to BigDecimal("20.00")
        )

        //and
        stubFor(
            get("/api/v1/accountancy/product-price")
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
        val responseActual = accountancyClient.getProductsPrice()

        //  then
        assertThat(responseActual.size).isEqualTo(1)
        assertThat(responseActual[1]).isEqualTo(BigDecimal("20.00"))
    }
}
