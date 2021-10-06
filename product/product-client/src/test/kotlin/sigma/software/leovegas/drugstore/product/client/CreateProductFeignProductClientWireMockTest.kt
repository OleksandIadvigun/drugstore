package sigma.software.leovegas.drugstore.product.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.matching.ContainsPattern
import com.github.tomakehurst.wiremock.matching.EqualToPattern
import java.math.BigDecimal
import java.time.LocalDateTime
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import sigma.software.leovegas.drugstore.product.api.CreateProductRequest
import sigma.software.leovegas.drugstore.product.api.CreateProductResponse
import sigma.software.leovegas.drugstore.product.api.CreateProductsEvent
import sigma.software.leovegas.drugstore.product.api.ProductStatusDTO

@SpringBootApplication
internal class CreateProductFeignProductClientWireMockTestApp

@DisplayName("Create Product Feign Client WireMock test")
@ContextConfiguration(classes = [CreateProductFeignProductClientWireMockTestApp::class])
class CreateProductFeignProductClientWireMockTest @Autowired constructor(
    val productClient: ProductClient,
    val objectMapper: ObjectMapper
) : WireMockTest() {

    @Test
    fun `should create product`() {

        // given
        val request = CreateProductsEvent(
            listOf(
                CreateProductRequest(
                    name = "test1",
                    quantity = 1,
                    price = BigDecimal.ONE
                )
            )
        )

        //and
        val responseExpected = listOf(
            CreateProductResponse(
                productNumber = "1",
                name = "test1",
                quantity = 1,
                price = BigDecimal.ONE,
                status = ProductStatusDTO.CREATED,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
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
        assertThat(responseActual[0].productNumber).isEqualTo("1")
        assertThat(responseActual[0].name).isEqualTo("test1")
        assertThat(responseActual[0].quantity).isEqualTo(1)
        assertThat(responseActual[0].price).isEqualTo(BigDecimal.ONE)
        assertThat(responseActual[0].status).isEqualTo(ProductStatusDTO.CREATED)
    }
}
