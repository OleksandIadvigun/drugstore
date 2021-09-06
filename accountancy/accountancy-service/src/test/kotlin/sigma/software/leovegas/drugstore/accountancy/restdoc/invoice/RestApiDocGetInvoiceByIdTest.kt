package sigma.software.leovegas.drugstore.accountancy.restdoc.invoice

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
import sigma.software.leovegas.drugstore.accountancy.Invoice
import sigma.software.leovegas.drugstore.accountancy.InvoiceRepository
import sigma.software.leovegas.drugstore.accountancy.client.AccountancyProperties
import sigma.software.leovegas.drugstore.accountancy.restdoc.RestApiDocumentationTest

@DisplayName("Get invoice  by id REST API Doc test")
class RestApiDocGetInvoiceByIdTest @Autowired constructor(
    @LocalServerPort val port: Int,
    val accountancyProperties: AccountancyProperties,
    val invoiceRepository: InvoiceRepository,
    val transactionTemplate: TransactionTemplate
) : RestApiDocumentationTest(accountancyProperties) {

    @Test
    fun `should get invoice by id`() {

        // given
        transactionTemplate.execute {
            invoiceRepository.deleteAll()
        }

        // given
        val savedInvoice = transactionTemplate.execute {
            invoiceRepository.save(
                Invoice(
                    orderId = 1L,
                    total = BigDecimal("90.00")
                )
            )
        } ?: fail("result is expected")

        of("get-invoice-by-id").`when`()
            .pathParam("id", savedInvoice.id)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .get("http://${accountancyProperties.host}:$port/api/v1/accountancy/invoice/{id}")
            .then()
            .assertThat().statusCode(200)
            .assertThat().body("createdAt", not(emptyString()))
            .assertThat().body("total", equalTo(90.0F))
    }
}
