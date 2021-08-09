package sigma.software.leovegas.drugstore.product.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.matching.ContainsPattern
import java.math.BigDecimal
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import sigma.software.leovegas.drugstore.product.api.ProductResponse

@SpringBootApplication
internal class GetProductByIdFeignClientWireMockTestApp

@DisplayName("Get Product By Id Feign Client WireMock test")
@ContextConfiguration(classes = [GetProductByIdFeignClientWireMockTestApp::class])
class GetProductByIdFeignClientWireMockTest @Autowired constructor(
    val productClient: ProductClient,
    val objectMapper: ObjectMapper
) : WireMockTest() {

    @Test
    fun `should get product by id`() {

        // given
        val responseExpected = ProductResponse(
            id = 1L,
            name = "test",
            price = BigDecimal("20.00")
        )

        //and
        WireMock.stubFor(
            WireMock.get("/api/v1/products/1")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(
                    WireMock.aResponse()
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
        val responseActual = productClient.getProductById(1)

        //  then
        Assertions.assertThat(responseActual.id).isEqualTo(1L)
        Assertions.assertThat(responseActual.name).isEqualTo(responseExpected.name)
        Assertions.assertThat(responseActual.price).isEqualTo(responseExpected.price)
    }
}