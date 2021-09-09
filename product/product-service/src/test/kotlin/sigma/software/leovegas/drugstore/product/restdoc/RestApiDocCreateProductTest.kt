package sigma.software.leovegas.drugstore.product.restdoc

import com.fasterxml.jackson.databind.ObjectMapper
import java.math.BigDecimal
import org.hamcrest.Matchers.emptyString
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.nullValue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.MediaType
import sigma.software.leovegas.drugstore.product.ProductProperties
import sigma.software.leovegas.drugstore.product.api.CreateProductRequest

@DisplayName("Create product REST API Doc test")
class RestApiDocCreateProductTest @Autowired constructor(
    val objectMapper: ObjectMapper,
    @LocalServerPort val port: Int,
    val productProperties: ProductProperties,
) : RestApiDocumentationTest(productProperties) {

    @Test
    fun `should create product`() {

        // given
        val body = objectMapper
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(
                listOf(
                    CreateProductRequest(
                        name = "test",
                        price = BigDecimal("20.00"),
                        quantity = 5
                    )
                )
            )

        of("create-product").`when`()
            .body(body)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .post("http://${productProperties.host}:$port/api/v1/products")
            .then()
            .assertThat().statusCode(201)
            .assertThat().body("[0].id", not(nullValue()))
            .assertThat().body("[0].name", equalTo("test"))
            .assertThat().body("[0].createdAt", not(emptyString()))
            .assertThat().body("[0].updatedAt", not(emptyString()))
    }
}
