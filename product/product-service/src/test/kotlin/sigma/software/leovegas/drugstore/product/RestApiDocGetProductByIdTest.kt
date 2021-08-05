package sigma.software.leovegas.drugstore.product

import java.math.BigDecimal
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.transaction.support.TransactionTemplate

@DisplayName("Get product by id REST API Doc test")
class RestApiDocGetProductByIdTest(
    @Autowired @LocalServerPort val port: Int,
    @Autowired val transactionTemplate: TransactionTemplate,
    @Autowired val productService: ProductService
) : RestApiDocumentationTest() {


    @Test
    fun `should get product by id`() {

        // given
        val newProduct = ProductRequest(
            name = "test",
            price = BigDecimal.TEN.setScale(2),
        )

        // and
        val savedProduct = transactionTemplate.execute {
            productService.create(newProduct)
        } ?: kotlin.test.fail("result is expected")

        of("get-product-by-id")
            .pathParam("id", savedProduct.id)
            .`when`()
            .get("http://localhost:$port/api/v1/products/{id}")
            .then()
            .assertThat().statusCode(200)
            .assertThat().body("name",equalTo("test"))
            .assertThat().body("price",equalTo(10.0F))


    }
}
