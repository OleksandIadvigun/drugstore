package sigma.software.leovegas.drugstore.product

import com.fasterxml.jackson.databind.ObjectMapper
import java.math.BigDecimal
import org.hamcrest.Matchers.emptyString
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.MediaType
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.product.api.ProductRequest

@DisplayName("Update order REST API Doc test")
class RestApiDocUpdateProductTest @Autowired constructor(
    @LocalServerPort val port: Int,
    val service: ProductService,
    val objectMapper: ObjectMapper,
    val transactionTemplate: TransactionTemplate,
    val productProperties: ProductProperties
) : RestApiDocumentationTest() {

    @Test
    fun `should update product`() {

        val savedProduct = transactionTemplate.execute {
            service.create(
                ProductRequest(
                    name = "test",
                    price = BigDecimal.ONE,
                )
            )
        } ?: fail("result is expected")

        // and
        val orderJson = objectMapper
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(
                ProductRequest(
                    name = "test product edited",
                    price = BigDecimal.TEN
                )
            )

        of("update-product")
            .`when`()
            .body(orderJson)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .put("http://${productProperties.host}:$port/api/v1/products/${savedProduct.id}")
            .then()
            .assertThat().statusCode(202)
            .assertThat().body("name", equalTo("test product edited"))
            .assertThat().body("price", equalTo(10))
            .assertThat().body("createdAt", not(emptyString()))
            .assertThat().body("updatedAt", not(emptyString()))
    }
}
