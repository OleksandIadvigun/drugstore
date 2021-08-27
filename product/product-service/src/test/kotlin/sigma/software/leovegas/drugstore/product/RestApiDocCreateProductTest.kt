package sigma.software.leovegas.drugstore.product

import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.Matchers.emptyString
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.nullValue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.MediaType
import sigma.software.leovegas.drugstore.product.api.ProductRequest

@DisplayName("Create product REST API Doc test")
class RestApiDocCreateProductTest @Autowired constructor(
    val objectMapper: ObjectMapper,
    @LocalServerPort val port: Int,
    val productProperties: ProductProperties,
) : RestApiDocumentationTest(productProperties) {

    @Test
    fun `should create product`() {
        // given
        val orderJson = objectMapper
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(
                ProductRequest(
                    name = "test product",
                )
            )

        of("create-product").`when`()
            .body(orderJson)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .post("http://${productProperties.host}:$port/api/v1/products")
            .then()
            .assertThat().statusCode(201)
            .assertThat().body("id", not(nullValue()))
            .assertThat().body("name", equalTo("test product"))
            .assertThat().body("createdAt", not(emptyString()))
            .assertThat().body("updatedAt", not(emptyString()))
    }
}
