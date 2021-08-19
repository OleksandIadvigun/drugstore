package sigma.software.leovegas.drugstore.product

import java.math.BigDecimal
import org.hamcrest.Matchers.emptyString
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.product.api.ProductRequest

@DisplayName("Get product by id REST API Doc test")
class RestApiDocGetProductByIdTest @Autowired constructor(
    @LocalServerPort val port: Int,
    val transactionTemplate: TransactionTemplate,
    val productService: ProductService,
    val productProperties: ProductProperties
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
        } ?: fail("result is expected")

        of("get-product-by-id")
            .pathParam("id", savedProduct.id)
            .`when`()
            .get("http://${productProperties.host}:$port/api/v1/products/{id}")
            .then()
            .assertThat().statusCode(200)
            .assertThat().body("name", equalTo("test"))
            .assertThat().body("price", equalTo(10.0F))
            .assertThat().body("createdAt", not(emptyString()))
            .assertThat().body("updatedAt", not(emptyString()))


    }
}
