package sigma.software.leovegas.drugstore.accountancy

import java.math.BigDecimal
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.MediaType
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.accountancy.client.AccountancyProperties

@DisplayName("Get products price by ids REST API Doc test")
class RestApiDocGetPriceItemsByIdsTest @Autowired constructor(
    @LocalServerPort val port: Int,
    val transactionTemplate: TransactionTemplate,
    val accountancyProperties: AccountancyProperties,
    val priceItemRepo: PriceItemRepository
) : RestApiDocumentationTest(accountancyProperties) {


    @Test
    fun `should get price items by ids`() {

        //given
        transactionTemplate.execute {
            priceItemRepo.deleteAll()
        }

        // and
        val ids = transactionTemplate.execute {
            priceItemRepo.saveAll(
                listOf(
                    PriceItem(
                        productId = 1L,
                        price = BigDecimal("10.00")
                    ),
                    PriceItem(
                        productId = 2L,
                        price = BigDecimal("20.00")
                    )
                )
            )
        }?.map { it.id } ?: fail("Fail, response is expected")

        // then
        of("get-price-items-by-ids").`when`()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .get("http://${accountancyProperties.host}:$port/api/v1/accountancy/price-items-by-ids?ids=${ids[0]},${ids[1]}")
            .then()
            .assertThat().statusCode(200)
            .assertThat().body("size()", equalTo(2))
            .assertThat().body("[0].price", equalTo(10.0F))
            .assertThat().body("[1].price", equalTo(20.0F))
    }
}
