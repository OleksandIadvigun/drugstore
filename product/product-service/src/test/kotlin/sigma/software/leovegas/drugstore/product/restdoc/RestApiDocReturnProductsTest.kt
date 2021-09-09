package sigma.software.leovegas.drugstore.product.restdoc

import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.MediaType
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.infrastructure.extensions.get
import sigma.software.leovegas.drugstore.product.Product
import sigma.software.leovegas.drugstore.product.ProductProperties
import sigma.software.leovegas.drugstore.product.ProductRepository
import sigma.software.leovegas.drugstore.product.api.ReturnProductQuantityRequest

@DisplayName("Return products quantity REST API Doc test")
class RestApiDocReturnProductsTest @Autowired constructor(
    @LocalServerPort val port: Int,
    val transactionTemplate: TransactionTemplate,
    val productRepository: ProductRepository,
    val productProperties: ProductProperties,
    val objectMapper: ObjectMapper,
) : RestApiDocumentationTest(productProperties) {


    @Test
    fun `should reduce products quantity`() {

        // given
        transactionTemplate.execute {
            productRepository.deleteAllInBatch()
        }

        // given
        val saved = transactionTemplate.execute {
            productRepository.save(
                Product(
                    name = "test",
                    quantity = 10
                )
            )
        }.get()

        // and
        val body = objectMapper
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(
                listOf(
                    ReturnProductQuantityRequest(
                        id = saved.id ?: -1,
                        quantity = 3
                    )
                )
            )

        of("return-products").`when`()
            .body(body)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .put("http://${productProperties.host}:$port/api/v1/products/return")
            .then()
            .assertThat().statusCode(202)
            .assertThat().body("size()", `is`(1))
            .assertThat().body("[0].quantity", equalTo(13))
    }
}
