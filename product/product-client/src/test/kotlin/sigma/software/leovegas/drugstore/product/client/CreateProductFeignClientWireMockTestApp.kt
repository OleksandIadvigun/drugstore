package sigma.software.leovegas.drugstore.product.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.post
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
import sigma.software.leovegas.drugstore.product.api.ProductRequest
import sigma.software.leovegas.drugstore.product.api.ProductResponse

@SpringBootApplication
internal class CreateProductFeignClientWireMockTestApp

@DisplayName("Create Product Feign Client WireMock test")
@ContextConfiguration(classes = [CreateProductFeignClientWireMockTestApp::class])
class CreateProductFeignClientWireMockTest @Autowired constructor(
    val productClient: ProductClient,
    val objectMapper: ObjectMapper
) : WireMockTest() {

    @Test
    fun `should create product`() {

        // given
        val request = ProductRequest(
            name = "test",
            price = BigDecimal("20.00")
        )

        //and
        val responseExpected = ProductResponse(
            id = 1L,
            name = request.name,
            price = request.price
        )

        //and
        stubFor(
            post("/api/v1/products")
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
        val responseActual = productClient.createProduct(request)

        //  then
        assertThat(responseActual.id).isEqualTo(1L)
        assertThat(responseActual.name).isEqualTo(request.name)
        assertThat(responseActual.price).isEqualTo(request.price)
    }
}