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
import sigma.software.leovegas.drugstore.product.api.ProductDetailsResponse

@SpringBootApplication
internal class GetProductsDetailsByIdsFeignClientWireMockTestApp

@DisplayName("Get Products Details by Ids Feign Client WireMock test")
@ContextConfiguration(classes = [GetProductsDetailsByIdsFeignClientWireMockTestApp::class])
class GetProductsDetailsByIdsFeignClientWireMockTest @Autowired constructor(
    val productClient: ProductClient,
    val objectMapper: ObjectMapper
) : WireMockTest() {

    @Test
    fun `should get products by ids`() {

        val responseExpected = listOf(
            ProductDetailsResponse(
                productNumber = "1",
                name = "test1",
                quantity = 1,
                price = BigDecimal.ONE
            )
        )

        // and
        stubFor(
            get("/api/v1/products/details?productNumbers=1")
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
        val responseActual = productClient.getProductsDetailsByProductNumbers(listOf("1"))

        //  then
        assertThat(responseActual.size).isEqualTo(1)
        assertThat(responseActual[0].productNumber).isEqualTo("1")
        assertThat(responseActual[0].name).isEqualTo("test1")
        assertThat(responseActual[0].price).isEqualTo(BigDecimal.ONE)
        assertThat(responseActual[0].quantity).isEqualTo(1)
    }
}
