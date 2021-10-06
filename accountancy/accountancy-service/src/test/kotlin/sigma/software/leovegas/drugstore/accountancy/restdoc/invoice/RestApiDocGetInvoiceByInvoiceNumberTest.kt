package sigma.software.leovegas.drugstore.accountancy.restdoc.invoice

import java.math.BigDecimal
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.MediaType
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.accountancy.Invoice
import sigma.software.leovegas.drugstore.accountancy.InvoiceRepository
import sigma.software.leovegas.drugstore.accountancy.ProductItem
import sigma.software.leovegas.drugstore.accountancy.client.AccountancyProperties
import sigma.software.leovegas.drugstore.accountancy.restdoc.RestApiDocumentationTest
import sigma.software.leovegas.drugstore.extensions.get

@DisplayName("Get invoice by invoice number REST API Doc test")
class RestApiDocGetInvoiceByInvoiceNumberTest @Autowired constructor(
    val accountancyProperties: AccountancyProperties,
    val transactionTemplate: TransactionTemplate,
    val invoiceRepository: InvoiceRepository,
    @LocalServerPort val port: Int,
) : RestApiDocumentationTest(accountancyProperties) {

    @Test
    fun `should get invoice by invoice number`() {

        // setup
        transactionTemplate.execute { invoiceRepository.deleteAll() }

        // given
        val savedInvoice = transactionTemplate.execute {
            invoiceRepository.save(
                Invoice(
                    invoiceNumber = "1",
                    orderNumber = "1",
                    total = BigDecimal("90.00"),
                    productItems = setOf(
                        ProductItem(
                            name = "test",
                            price = BigDecimal("30"),
                            quantity = 3
                        )
                    )
                )
            )
        }.get()

        of("get-invoice-by-invoice-number").`when`()
            .pathParam("invoice_number", savedInvoice.invoiceNumber)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .get("http://${accountancyProperties.host}:$port/api/v1/accountancy/invoice/{invoice_number}")
            .then()
            .assertThat().statusCode(200)
            .assertThat().body("amount", equalTo(90.0F))
            .assertThat().body("orderNumber", equalTo("1"))
            .assertThat().body("status", equalTo("NONE"))
    }
}
