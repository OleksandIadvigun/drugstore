package sigma.software.leovegas.drugstore.product

import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.transaction.support.TransactionTemplate

@DisplayName("Get products by Ids REST API Doc test")
class RestApiDocGetProductsByIdsTest @Autowired constructor(
    @LocalServerPort val port: Int,
    val transactionTemplate: TransactionTemplate,
    val productRepository: ProductRepository,
    val productProperties: ProductProperties,
) : RestApiDocumentationTest() {


    @Test
    fun `should get products by ids`() {

        // given
        transactionTemplate.execute {
            productRepository.deleteAllInBatch()
        }

        // given
        val ids = transactionTemplate.execute {
            productRepository.saveAll(
                listOf(
                    Product(
                        name = "test1",
                    ),
                    Product(
                        name = "test2",
                    )
                )
            ).map { it.id ?: -1 }.toList()
        } ?: listOf(-1L)

        of("get-products-by-ids").`when`()
            .get("http://${productProperties.host}:$port/api/v1/products-by-ids/?ids=${ids[0]}&ids=${ids[1]}")
            .then()
            .assertThat().statusCode(200)
            .assertThat().body("size()", `is`(2))

    }
}
