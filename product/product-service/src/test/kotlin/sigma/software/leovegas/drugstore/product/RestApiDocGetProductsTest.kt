package sigma.software.leovegas.drugstore.product

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.matching.ContainsPattern
import java.math.BigDecimal
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

@AutoConfigureWireMock(port=8082)
@DisplayName("Get products REST API Doc test")
class RestApiDocGetProductsTest(
    @Autowired @LocalServerPort val port: Int,
    @Autowired val transactionTemplate: TransactionTemplate,
    @Autowired val productService: ProductService,
    @Autowired val productRepository: ProductRepository,
    @Autowired val objectMapper: ObjectMapper
) :RestApiDocumentationTest() {


    @Test
    fun `should get products`() {

        // given
        transactionTemplate.execute {
            productRepository.deleteAll()
        }

        // and
        val savedProducts = transactionTemplate.execute {
            productRepository.saveAll(listOf(
                Product(
                    name = "test",
                    price = BigDecimal.TEN.setScale(2),
                ),
                Product(
                    name = "test2",
                    price = BigDecimal.TEN.setScale(2),
                )
            ))
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
                                .writeValueAsString(mapOf(savedProducts[0].id to 3, savedProducts[1].id to 5))
                        )
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                )
        )

            of("get-products").`when`()
                .get("http://localhost:$port/api/v1/products")
                .then()
                .assertThat().statusCode(200)
                .assertThat().body("content.size", `is`(2))
    }
}
