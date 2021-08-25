package sigma.software.leovegas.drugstore.accountancy

import com.fasterxml.jackson.databind.ObjectMapper
import java.math.BigDecimal
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.MediaType
import sigma.software.leovegas.drugstore.accountancy.api.PriceItemRequest
import sigma.software.leovegas.drugstore.accountancy.client.AccountancyProperties

@DisplayName("Create price item REST API Doc test")
class RestApiDocCreatePriceItemTest @Autowired constructor(
    val objectMapper: ObjectMapper,
    @LocalServerPort val port: Int,
    val accountancyProperties: AccountancyProperties
) : RestApiDocumentationTest() {

    @Test
    fun `should create price item`() {

        // given
        val priceItemJson = objectMapper
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(
                PriceItemRequest(
                    productId = 1L,
                    price = BigDecimal("20.00")
                )
            )

        of("create-price-item").`when`()
            .body(priceItemJson)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .post("http://${accountancyProperties.host}:$port/api/v1/accountancy/price-item")
            .then()
            .assertThat().statusCode(201)
            .assertThat().body("productId", equalTo(1))
            .assertThat().body("price", equalTo(20.0F))
    }
}
