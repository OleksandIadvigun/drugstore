package sigma.software.leovegas.drugstore.product.restdoc

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.matching.ContainsPattern
import java.math.BigDecimal
import org.hamcrest.Matchers
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.infrastructure.extensions.get
import sigma.software.leovegas.drugstore.product.Product
import sigma.software.leovegas.drugstore.product.ProductProperties
import sigma.software.leovegas.drugstore.product.ProductRepository
import sigma.software.leovegas.drugstore.product.ProductStatus

@DisplayName("Get products REST API Doc test")
class RestApiDocGetProductsTest @Autowired constructor(
    @LocalServerPort val port: Int,
    val transactionTemplate: TransactionTemplate,
    val productRepository: ProductRepository,
    val objectMapper: ObjectMapper,
    val productProperties: ProductProperties
) : RestApiDocumentationTest(productProperties) {


    @Test
    fun `should get products by search sorted by popularity  `() {

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
                        name = "test3",
                        price = BigDecimal("10.00"),
                        quantity = 7,
                        status = ProductStatus.RECEIVED,
                    )
                )
            )
        }.get()

        // and
        stubFor(
            WireMock.get("/api/v1/orders/total-buys")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(
                    aResponse()
                        .withBody(
                            objectMapper
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(mapOf(savedProducts[1].id to 5, savedProducts[0].id to 1))
                        )
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                )
        )

        // and
        stubFor(
            WireMock.get("/api/v1/accountancy/sale-price?ids=${savedProducts[0].id}&ids=${savedProducts[1].id}")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(
                    aResponse()
                        .withBody(
                            objectMapper
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(
                                    mapOf(
                                        Pair(savedProducts[1].id, BigDecimal("100.00")),
                                        Pair(savedProducts[0].id, BigDecimal("20.00"))
                                    )
                                )
                        )
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                )
        )

        of("get-products").`when`()
            .get("http://${productProperties.host}:$port/api/v1/products/search?search=test")
            .then()
            .assertThat().statusCode(200)
            .assertThat().body("size", `is`(2))
            .assertThat().body("[0].name", Matchers.equalTo("test2"))
            .assertThat().body("[0].price", Matchers.equalTo(100.0F))
            .assertThat().body("[0].quantity", Matchers.equalTo(3))
            .assertThat().body("[1].name", Matchers.equalTo("test"))
    }
}
