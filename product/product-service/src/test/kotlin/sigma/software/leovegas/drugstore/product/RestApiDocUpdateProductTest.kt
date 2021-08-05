package sigma.software.leovegas.drugstore.product

import com.fasterxml.jackson.databind.ObjectMapper
import java.math.BigDecimal
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.MediaType
import org.springframework.transaction.support.TransactionTemplate

@DisplayName("Update order REST API Doc test")
class RestApiDocUpdateProductTest(
    @Autowired @LocalServerPort val port: Int,
    @Autowired val service: ProductService,
    @Autowired val objectMapper: ObjectMapper,
    @Autowired val transactionTemplate: TransactionTemplate,
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
        } ?: kotlin.test.fail("result is expected")
        println(savedProduct)
        // and
        val orderJson = objectMapper
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(
                ProductRequest(
                    name = "test product edited",
                    price = BigDecimal.TEN
                )
            )
        println(orderJson)

        of("update-product")
            .`when`()
            .body(orderJson)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .put("http://localhost:$port/api/v1/products/${savedProduct.id}")
            .then()
            .assertThat().statusCode(202)
            .assertThat().body("name", equalTo("test product edited"))
            .assertThat().body("price", equalTo(10))


    }
}
