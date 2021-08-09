package sigma.software.leovegas.drugstore.product

import java.math.BigDecimal
import org.hamcrest.Matchers
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.product.api.ProductRequest

@DisplayName("Get products REST API Doc test")
class RestApiDocGetProductsTest(
    @Autowired @LocalServerPort val port: Int,
    @Autowired val transactionTemplate: TransactionTemplate,
    @Autowired val productService: ProductService,
    @Autowired val productRepository: ProductRepository
) :RestApiDocumentationTest() {


    @Test
    fun `should get products`() {

        // given
        transactionTemplate.execute {
            productRepository.deleteAll()
        }

        // given
        val newProduct = ProductRequest(
            name = "test",
            price = BigDecimal.TEN.setScale(2),
        )

        // and
        val savedProduct = transactionTemplate.execute {
            productService.create(newProduct)
        } ?: fail("result is expected")


            of("get-products").`when`()
                .get("http://localhost:$port/api/v1/products")
                .then()
                .assertThat().statusCode(200)
                .assertThat().body("size()", Matchers.`is`(1))

    }
}