package sigma.software.leovegas.drugstore.accountancy.restdoc.purchasedcosts

import com.fasterxml.jackson.databind.ObjectMapper
import java.time.LocalDateTime
import org.assertj.core.api.Assertions.fail
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.MediaType
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.accountancy.PurchasedCosts
import sigma.software.leovegas.drugstore.accountancy.PurchasedCostsRepository
import sigma.software.leovegas.drugstore.accountancy.client.AccountancyProperties
import sigma.software.leovegas.drugstore.accountancy.restdoc.RestApiDocumentationTest

@DisplayName("Create purchased costs REST API Doc test")
class RestApiDocGetPurchasedCostsTest @Autowired constructor(
    val objectMapper: ObjectMapper,
    @LocalServerPort val port: Int,
    val accountancyProperties: AccountancyProperties,
    val transactionTemplate: TransactionTemplate,
    val purchasedCostsRepository: PurchasedCostsRepository
) : RestApiDocumentationTest(accountancyProperties) {

    @Test
    fun `should get purchased costs`() {

        // given
        transactionTemplate.execute {
            purchasedCostsRepository.deleteAll()
        }

        // and
        val purchasedCosts = transactionTemplate.execute {
            purchasedCostsRepository.saveAll(
                listOf(
                    PurchasedCosts(
                        priceItemId = 1,
                        quantity = 5,
                    ),
                    PurchasedCosts(
                        priceItemId = 2,
                        quantity = 5,
                    )
                )
            )
        } ?: fail("result is expected")

        // and
        val dateFrom = LocalDateTime.now().minusDays(1)
        val dateTo = LocalDateTime.now().plusDays(1)

        of("get-purchased-costs").`when`()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .get(
                "http://${accountancyProperties.host}:$port" +
                        "/api/v1/accountancy/purchased-costs?dateFrom=${dateFrom}&dateTo=${dateTo}"
            )
            .then()
            .assertThat().statusCode(200)
            .assertThat().body("size", equalTo(2))
    }
}
