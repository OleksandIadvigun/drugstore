package sigma.software.leovegas.drugstore.product.client

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
import sigma.software.leovegas.drugstore.product.api.ProductResponse

@SpringBootApplication
internal class GetProductsByIdsFeignClientWireMockTestApp

@DisplayName("Get Products by Ids Feign Client WireMock test")
@ContextConfiguration(classes = [GetProductsByIdsFeignClientWireMockTestApp::class])
class GetProductsByIdsFeignClientWireMockTest @Autowired constructor(
    val productClient: ProductClient,
    val objectMapper: ObjectMapper
) : WireMockTest() {

    @Test
    fun `should get products by ids`() {

        // given
        val responseExpected: List<ProductResponse> = listOf(
            ProductResponse(
                name = "test",
                price = BigDecimal("20.00")
            ),
            ProductResponse(
                name = "test2",
                price = BigDecimal("40.00")
            )
        )

        //and
        stubFor(
            get("/api/v1/products-by-ids/?ids=1&ids=2")
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
        val responseActual = productClient.getProductsByIds((listOf(1L, 2L)))

        //  then
        assertThat(responseActual.size).isEqualTo(2)
    }
}
