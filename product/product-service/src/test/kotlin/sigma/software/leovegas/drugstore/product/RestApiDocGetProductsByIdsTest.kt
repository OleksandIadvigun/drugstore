package sigma.software.leovegas.drugstore.product

import java.math.BigDecimal
import org.hamcrest.Matchers
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.transaction.support.TransactionTemplate

@DisplayName("Get products by Ids REST API Doc test")
class RestApiDocGetProductsByIdsTest(
    @Autowired @LocalServerPort val port: Int,
    @Autowired val transactionTemplate: TransactionTemplate,
    @Autowired val productRepository: ProductRepository
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
                        price = BigDecimal("20.00")
                    ),
                    Product(
                        name = "test2",
                        price = BigDecimal("40.00")
                    )
                )
            ).map { it.id ?: -1 }.toList()
        } ?: listOf(-1L)

        of("get-products-by-ids").`when`()
            .get("http://localhost:$port/api/v1/products-by-ids/?ids=${ids[0]}&ids=${ids[1]}")
            .then()
            .assertThat().statusCode(200)
            .assertThat().body("size()", Matchers.`is`(2))

    }
}