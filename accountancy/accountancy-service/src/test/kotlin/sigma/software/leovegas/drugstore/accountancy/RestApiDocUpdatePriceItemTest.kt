package sigma.software.leovegas.drugstore.accountancy

import com.fasterxml.jackson.databind.ObjectMapper
import java.math.BigDecimal
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.MediaType
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.accountancy.api.PriceItemRequest
import sigma.software.leovegas.drugstore.accountancy.client.AccountancyProperties

@DisplayName("Update price item REST API Doc test")
class RestApiDocUpdatePriceItemTest @Autowired constructor(
    val objectMapper: ObjectMapper,
    @LocalServerPort val port: Int,
    val transactionTemplate: TransactionTemplate,
    val accountancyProperties: AccountancyProperties,
    val accountancyService: AccountancyService
) : RestApiDocumentationTest() {


    @Test
    fun `should update price item`() {

        // given
        val priceItemCreated = transactionTemplate.execute {
            accountancyService.createPriceItem(
                PriceItemRequest(
                    productId = 1L,
                    price = BigDecimal("10.00")
                )
            )
        }

        // and
        val priceItemJson = objectMapper
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(
                PriceItemRequest(
                    productId = 1L,
                    price = BigDecimal("20.00")
                )
            )

        of("update-price-item").`when`()
            .body(priceItemJson)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .put("http://${accountancyProperties.host}:$port/api/v1/accountancy/price-item/${priceItemCreated?.id}")
            .then()
            .assertThat().statusCode(202)
            .assertThat().body("productId", equalTo(1))
            .assertThat().body("price", equalTo(20.0F))
    }
}
