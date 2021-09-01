package sigma.software.leovegas.drugstore.product

import com.fasterxml.jackson.databind.ObjectMapper
import java.math.BigDecimal
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.MediaType
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.product.api.ReduceProductQuantityRequest

@DisplayName("Reduce products quantity REST API Doc test")
class RestApiDocReduceQuantityProductTest @Autowired constructor(
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

        // and
        val ids = transactionTemplate.execute {
            productRepository.saveAll(
                listOf(
                    Product(
                        name = "test1",
                        price = BigDecimal("20.00"),
                        quantity = 5,
                        status = ProductStatus.RECEIVED,
                    ),
                    Product(
                        name = "test2",
                        price = BigDecimal("20.00"),
                        quantity = 3,
                        status = ProductStatus.RECEIVED,
                    )
                )
            ).map { it.id ?: -1 }.toList()
        } ?: listOf(-1L)

        // and
        val orderJson = objectMapper
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(
                listOf(
                    ReduceProductQuantityRequest(
                        id = ids[0],
                        quantity = 2
                    ),
                    ReduceProductQuantityRequest(
                        id = ids[1],
                        quantity = 2
                    )
                )
            )

        of("reduce-products-quantity").`when`()
            .body(orderJson)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .put("http://${productProperties.host}:$port/api/v1/products/reduce-quantity")
            .then()
            .assertThat().statusCode(202)
            .assertThat().body("size()", `is`(2))
            .assertThat().body("[0].quantity", equalTo(3))
            .assertThat().body("[1].quantity", equalTo(1))
    }
}
