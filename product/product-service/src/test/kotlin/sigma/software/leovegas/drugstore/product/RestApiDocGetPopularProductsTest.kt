package sigma.software.leovegas.drugstore.product

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.matching.ContainsPattern
import java.math.BigDecimal
import org.hamcrest.Matchers
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.transaction.support.TransactionTemplate

@AutoConfigureWireMock(port = 8082)
@DisplayName("Get popular products REST API Doc test")
class RestApiDocGetPopularProductsTest @Autowired constructor(
    @LocalServerPort val port: Int,
    val transactionTemplate: TransactionTemplate,
    val productRepository: ProductRepository,
    val objectMapper: ObjectMapper,
    val productProperties: ProductProperties
) : RestApiDocumentationTest(productProperties) {


    @Test
    fun `should get popular products`() {

        // given
        transactionTemplate.execute {
            productRepository.deleteAll()
        }

        // and
        val savedProducts = transactionTemplate.execute {
            productRepository.saveAll(
                listOf(
                    Product(
                        name = "test",
                        price = BigDecimal("20.00"),
                        quantity = 5,
                        status = ProductStatus.RECEIVED,
                    ),
                    Product(
                        name = "test2",
                        price = BigDecimal("30.00"),
                        quantity = 3,
                        status = ProductStatus.RECEIVED,
                    ),
                    Product(
                        name = "mostPopular",
                        price = BigDecimal("10.00"),
                        quantity = 7,
                        status = ProductStatus.RECEIVED,
                    )
                )
            )
        } ?: fail("result is expected")

        // and
        stubFor(
            get("/api/v1/orders/total-buys")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(
                    aResponse()
                        .withBody(
                            objectMapper
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(
                                    mapOf(
                                        savedProducts[2].id to 8,
                                        savedProducts[0].id to 5,
                                        savedProducts[1].id to 2
                                    )
                                )
                        )
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                )
        )

        of("get-popular-products").`when`()
            .get("http://${productProperties.host}:$port/api/v1/products/popular")
            .then()
            .assertThat().statusCode(200)
            .assertThat().body("totalElements", `is`(3))
            .assertThat().body("content[0].name", Matchers.equalTo("mostPopular"))
            .assertThat().body("content[1].name", Matchers.equalTo("test"))
            .assertThat().body("content[2].name", Matchers.equalTo("test2"))
    }
}
