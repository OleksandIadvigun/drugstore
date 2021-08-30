package sigma.software.leovegas.drugstore.accountancy.restdoc.priceitem

import java.math.BigDecimal
import org.assertj.core.api.Assertions.fail
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.MediaType
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.accountancy.Invoice
import sigma.software.leovegas.drugstore.accountancy.InvoiceRepository
import sigma.software.leovegas.drugstore.accountancy.InvoiceStatus
import sigma.software.leovegas.drugstore.accountancy.ProductItem
import sigma.software.leovegas.drugstore.accountancy.client.AccountancyProperties
import sigma.software.leovegas.drugstore.accountancy.restdoc.RestApiDocumentationTest

@DisplayName("Get past purchased items REST API Doc test")
class RestApiDocGetPastPurchasedItemsTest @Autowired constructor(
    @LocalServerPort val port: Int,
    val transactionTemplate: TransactionTemplate,
    val accountancyProperties: AccountancyProperties,
    val invoiceRepository: InvoiceRepository
) : RestApiDocumentationTest(accountancyProperties) {

    @Test
    fun `should get purchased items`() {

        // given
        transactionTemplate.execute {
            invoiceRepository.deleteAll()
        }

        // and
        val created = transactionTemplate.execute {
            invoiceRepository.saveAll(
                listOf(
                    Invoice(
                        orderId = 1,
                        total = BigDecimal("50.00"),
                        status = InvoiceStatus.PAID,
                        productItems = setOf(
                            ProductItem(
                                priceItemId = 1,
                                name = "test1",
                                price = BigDecimal.TEN,
                                quantity = 5
                            )
                        )
                    ),
                    Invoice(
                        orderId = 2,
                        total = BigDecimal("50.00"),
                        status = InvoiceStatus.PAID,
                        productItems = setOf(
                            ProductItem(
                                priceItemId = 1,
                                name = "test1",
                                price = BigDecimal.TEN,
                                quantity = 5
                            )
                        )
                    ),
                    Invoice(
                        orderId = 3,
                        total = BigDecimal("20.00"),
                        status = InvoiceStatus.PAID,
                        productItems = setOf(
                            ProductItem(
                                priceItemId = 2,
                                name = "test1",
                                price = BigDecimal.ONE,
                                quantity = 2
                            )
                        )
                    ),
                    Invoice(
                        orderId = 4,
                        total = BigDecimal("20.00"),
                        status = InvoiceStatus.CREATED,
                        productItems = setOf(
                            ProductItem(
                                priceItemId = 2,
                                name = "test1",
                                price = BigDecimal.TEN,
                                quantity = 2
                            )
                        )
                    )
                )
            )
        } ?: fail("result is expected")

        // then
        of("get-past-purchased-items").`when`()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .get("http://${accountancyProperties.host}:$port/api/v1/accountancy/past-purchased-items")
            .then()
            .assertThat().statusCode(200)
            .assertThat().body("size()", equalTo(2))
            .assertThat().body("[0].quantity", equalTo(10))
            .assertThat().body("[1].quantity", equalTo(2))
    }
}
