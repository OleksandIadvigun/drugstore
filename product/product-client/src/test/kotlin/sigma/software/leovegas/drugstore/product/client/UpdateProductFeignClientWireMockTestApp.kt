package sigma.software.leovegas.drugstore.product.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.matching.ContainsPattern
import com.github.tomakehurst.wiremock.matching.EqualToPattern
import java.math.BigDecimal
import org.assertj.core.api.Assertions
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
internal class UpdateProductFeignClientWireMockTestApp

@DisplayName("Update Product Feign Client WireMock test")
@ContextConfiguration(classes = [UpdateProductFeignClientWireMockTestApp::class])
class UpdateProductFeignClientWireMockTest @Autowired constructor(
    val productClient: ProductClient,
    val objectMapper: ObjectMapper
) : WireMockTest() {

    @Test
    fun `should update product`() {

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
        WireMock.stubFor(
            WireMock.put("/api/v1/products/1")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
                .withRequestBody(
                    EqualToPattern(
                        objectMapper
                            .writerWithDefaultPrettyPrinter()
                            .writeValueAsString(request)
                    )
                )
                .willReturn(
                    WireMock.aResponse()
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
        val responseActual = productClient.updateProduct(1, request)

        //  then
        Assertions.assertThat(responseActual.id).isEqualTo(1L)
        Assertions.assertThat(responseActual.name).isEqualTo(request.name)
        Assertions.assertThat(responseActual.price).isEqualTo(request.price)
    }
}