package sigma.software.leovegas.drugstore.accountancy

import com.fasterxml.jackson.databind.ObjectMapper
import java.math.BigDecimal
import org.hamcrest.Matchers.emptyString
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.MediaType
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.accountancy.client.AccountancyProperties

@DisplayName("Get markups REST API Doc test")
class RestApiDocGetMarkupsOfPriceItemsTest @Autowired constructor(
    val objectMapper: ObjectMapper,
    @LocalServerPort val port: Int,
    val transactionTemplate: TransactionTemplate,
    val accountancyProperties: AccountancyProperties,
    val priceItemRepo: PriceItemRepository
) : RestApiDocumentationTest(accountancyProperties) {

    @Test
    fun `should get markups `() {

        //given
        transactionTemplate.execute {
            priceItemRepo.deleteAll()
        }

        // and
        transactionTemplate.execute {
            priceItemRepo.saveAll(
                listOf(
                    PriceItem(
                        productId = 1L,
                        price = BigDecimal("10.00"),
                        markup = BigDecimal("10.00")
                    ),
                    PriceItem(
                        productId = 2L,
                        price = BigDecimal("10.00"),
                        markup = BigDecimal("20.00")
                    )
                )
            )
        }

        // given

        of("get-markups").`when`()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .get("http://${accountancyProperties.host}:$port/api/v1/accountancy/price-item/markup")
            .then()
            .assertThat().statusCode(200)
            .assertThat().body("size()", equalTo(2))
            .assertThat().body("[0].markup", equalTo(10.0F))
            .assertThat().body("[1].markup", equalTo(20.0F))
    }
}
