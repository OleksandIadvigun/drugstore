package sigma.software.leovegas.drugstore.accountancy

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
import sigma.software.leovegas.drugstore.accountancy.client.AccountancyProperties

@DisplayName("Get products price by products ids REST API Doc test")
class RestApiDocGetProductsPriceByProductsIdsTest @Autowired constructor(
    val objectMapper: ObjectMapper,
    @LocalServerPort val port: Int,
    val transactionTemplate: TransactionTemplate,
    val accountancyProperties: AccountancyProperties,
    val priceItemRepo: PriceItemRepository
) : RestApiDocumentationTest() {


    @Test
    fun `should get products price by products ids`() {

        //given
        transactionTemplate.execute {
            priceItemRepo.deleteAll()
        }

        // and
        val saved = transactionTemplate.execute {
            priceItemRepo.saveAll(
                listOf(
                    PriceItem(
                        productId = 1L,
                        price = BigDecimal("10.00")
                    ),
                    PriceItem(
                        productId = 2L,
                        price = BigDecimal("10.00")
                    ),
                    PriceItem(
                        productId = 3L,
                        price = BigDecimal("10.00")
                    )
                )
            )
        } ?: fail("Fail, response is expected")

        // then
        of("get-products-price-by-products-ids").`when`()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .get("http://${accountancyProperties.host}:$port/api/v1/accountancy/product-price-by-ids?ids=1,2")
            .then()
            .assertThat().statusCode(200)
            .assertThat().body("size()", equalTo(2))
            .assertThat().body("[0].createdAt", not(emptyString()))
            .assertThat().body("[0].updatedAt", not(emptyString()))
    }
}
