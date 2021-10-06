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
import sigma.software.leovegas.drugstore.infrastructure.WireMockTest
import sigma.software.leovegas.drugstore.product.api.SearchProductResponse

@SpringBootApplication
internal class SearchProductsFeignProductClientWireMockTestApp

@DisplayName("Search Products Feign ProductClient WireMock test")
@ContextConfiguration(classes = [SearchProductsFeignProductClientWireMockTestApp::class])
class SearchProductsFeignProductClientWireMockTest @Autowired constructor(
    val productClient: ProductClient,
    val objectMapper: ObjectMapper
) : WireMockTest() {

    @Test
    fun `should search products by search word`() {

        // given
        val responseExpected = listOf(
            SearchProductResponse(
                productNumber = "1",
                name = "aspirin",
                price = BigDecimal.ONE,
                quantity = 1
            )
        )

        // and
        stubFor(
            get("/api/v1/products/search?page=0&size=5&search=aspirin&sortField=popularity&sortDirection=DESC")
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
        val responseActual = productClient.searchProducts(search = "aspirin")

        // then
        assertThat(responseActual).hasSize(1)
        assertThat(responseActual[0].productNumber).isEqualTo("1")
    }
}
