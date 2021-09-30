package sigma.software.leovegas.drugstore.product.restdoc

import com.fasterxml.jackson.databind.ObjectMapper
import java.math.BigDecimal
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.MediaType
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.product.Product
import sigma.software.leovegas.drugstore.product.ProductProperties
import sigma.software.leovegas.drugstore.product.ProductRepository
import sigma.software.leovegas.drugstore.product.ProductStatus

@DisplayName("Receive products REST API Doc test")
class RestApiDocReceiveProductsTest @Autowired constructor(
    @LocalServerPort val port: Int,
    val transactionTemplate: TransactionTemplate,
    val productRepository: ProductRepository,
    val productProperties: ProductProperties,
    val objectMapper: ObjectMapper,
) : RestApiDocumentationTest(productProperties) {

    @Disabled
    @Test
    fun `should receive products`() {

        // given
        transactionTemplate.execute {
            productRepository.deleteAllInBatch()
        }

        // and
        val productNumbers = transactionTemplate.execute {
            productRepository.saveAll(
                listOf(
                    Product(
                        productNumber = "1",
                        name = "test1",
                        price = BigDecimal("20.00"),
                        quantity = 5,
                        status = ProductStatus.CREATED,
                    ),
                    Product(
                        productNumber = "2",
                        name = "test2",
                        price = BigDecimal("20.00"),
                        quantity = 3,
                        status = ProductStatus.CREATED,
                    )
                )
            ).map { it.productNumber }
        }

        // and
        val body = objectMapper
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(productNumbers)

        of("receive-products").`when`()
            .body(body)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .put("http://${productProperties.host}:$port/api/v1/products/receive")
            .then()
            .assertThat().statusCode(202)
            .assertThat().body("size()", `is`(2))
            .assertThat().body("[0].status", equalTo("RECEIVED"))
            .assertThat().body("[1].status", equalTo("RECEIVED"))
    }
}
