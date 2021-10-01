package sigma.software.leovegas.drugstore.product.restdoc

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import java.math.BigDecimal
import org.hamcrest.Matchers
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.api.protobuf.Proto
import sigma.software.leovegas.drugstore.infrastructure.extensions.get
import sigma.software.leovegas.drugstore.infrastructure.extensions.withProtobufResponse
import sigma.software.leovegas.drugstore.product.Product
import sigma.software.leovegas.drugstore.product.ProductProperties
import sigma.software.leovegas.drugstore.product.ProductRepository
import sigma.software.leovegas.drugstore.product.ProductStatus

@DisplayName("Get popular products REST API Doc test")
class RestApiDocGetPopularProductsTest @Autowired constructor(
    @LocalServerPort val port: Int,
    val transactionTemplate: TransactionTemplate,
    val productRepository: ProductRepository,
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
                        productNumber = "1",
                        name = "test",
                        price = BigDecimal("20.00"),
                        quantity = 5,
                        status = ProductStatus.RECEIVED,
                    ),
                    Product(
                        productNumber = "2",
                        name = "test2",
                        price = BigDecimal("30.00"),
                        quantity = 3,
                        status = ProductStatus.RECEIVED,
                    ),
                    Product(
                        productNumber = "3",
                        name = "mostPopular",
                        price = BigDecimal("10.00"),
                        quantity = 7,
                        status = ProductStatus.RECEIVED,
                    )
                )
            )
        }.get()

        // and
        val responseExpected = Proto.ProductQuantityMap.newBuilder()
            .putProductQuantityItem(savedProducts[2].productNumber, 8)
            .putProductQuantityItem(savedProducts[0].productNumber, 5)
            .putProductQuantityItem(savedProducts[1].productNumber, 2)
            .build()

        // and
        stubFor(
            WireMock.get("/api/v1/orders/total-buys")
                .willReturn(
                    aResponse()
                        .withProtobufResponse { responseExpected }
                        .withStatus(HttpStatus.OK.value())
                )
        )

        of("get-popular-products").`when`()
            .get("http://${productProperties.host}:$port/api/v1/products/popular")
            .then()
            .assertThat().statusCode(200)
            .assertThat().body("size", `is`(3))
            .assertThat().body("[0].name", Matchers.equalTo("mostPopular"))
            .assertThat().body("[1].name", Matchers.equalTo("test"))
            .assertThat().body("[2].name", Matchers.equalTo("test2"))
    }
}
