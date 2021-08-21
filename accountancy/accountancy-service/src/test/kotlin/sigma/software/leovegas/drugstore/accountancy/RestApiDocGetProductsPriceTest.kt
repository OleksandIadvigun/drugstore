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

@DisplayName("Create Products Price REST API Doc test")
class RestApiDocGetProductsPriceTest @Autowired constructor(
    val objectMapper: ObjectMapper,
    @LocalServerPort val port: Int,
    val transactionTemplate: TransactionTemplate,
    val accountancyProperties: AccountancyProperties,
    val priceItemRepo: PriceItemRepository
) : RestApiDocumentationTest() {


    @Test
    fun `should get products price`() {

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
                        price = BigDecimal("10.00")
                    ),
                    PriceItem(
                        productId = 2L,
                        price = BigDecimal("10.00")
                    )
                )
            )
        }

        // given

        of("get-products-price").`when`()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .get("http://${accountancyProperties.host}:$port/api/v1/accountancy/product-price")
            .then()
            .assertThat().statusCode(200)
            .assertThat().body("size()", equalTo(2))
            .assertThat().body("[0].createdAt", not(emptyString()))
            .assertThat().body("[0].updatedAt", not(emptyString()))
    }
}
