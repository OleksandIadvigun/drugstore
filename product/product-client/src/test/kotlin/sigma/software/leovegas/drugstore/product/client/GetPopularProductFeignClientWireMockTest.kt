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
import sigma.software.leovegas.drugstore.product.api.GetProductResponse

@SpringBootApplication
internal class GetProductsFeignClientWireMockTestApp

@DisplayName("Get Product Feign Client WireMock tests")
@ContextConfiguration(classes = [GetProductsFeignClientWireMockTestApp::class])
class GetProductsFeignClientWireMockTests @Autowired constructor(
    val productClient: ProductClient,
    val objectMapper: ObjectMapper
) : WireMockTest() {

    @Test
    fun `should get product price`() {

        // given
        stubFor(
            get("/api/v1/products/1,2/price")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(
                    aResponse()
                        .withBody(
                            objectMapper
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(
                                    mapOf(Pair("1", BigDecimal("1.23")), Pair("2", BigDecimal("1.24")))
                                )
                        )
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                )
        )

        // when
        val priceMap = productClient.getProductPrice(listOf("1", "2"))

        // then
        assertThat(priceMap.getValue("1")).isEqualTo(BigDecimal("1.23"))
        assertThat(priceMap.getValue("2")).isEqualTo(BigDecimal("1.24"))
    }

    @Test
    fun `should get popular product`() {

        // given
        val responseExpected = listOf(
            GetProductResponse(
                productNumber = "1",
                name = "test1"
            ),
            GetProductResponse(
                productNumber = "2",
                name = "test2"
            )
        )

        // and
        stubFor(
            get("/api/v1/products/popular?page=0&size=5")
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
        val responseActual = productClient.getPopularProducts()

        // then
        assertThat(responseActual[0].productNumber).isEqualTo("1")
        assertThat(responseActual[0].name).isEqualTo("test1")
        assertThat(responseActual[1].productNumber).isEqualTo("2")
        assertThat(responseActual[1].name).isEqualTo("test2")
    }
}
