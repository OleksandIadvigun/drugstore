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
import sigma.software.leovegas.drugstore.accountancy.InvoiceStatus
import sigma.software.leovegas.drugstore.accountancy.InvoiceType
import sigma.software.leovegas.drugstore.accountancy.ProductItem
import sigma.software.leovegas.drugstore.accountancy.client.AccountancyProperties
import sigma.software.leovegas.drugstore.accountancy.restdoc.RestApiDocumentationTest
import sigma.software.leovegas.drugstore.extensions.get

@DisplayName("Get invoice details by order id REST API Doc test")
class RestApiDocGetInvoiceDetailsByOrderIdTest @Autowired constructor(
    @LocalServerPort val port: Int,
    val accountancyProperties: AccountancyProperties,
    val invoiceRepository: InvoiceRepository,
    val transactionTemplate: TransactionTemplate
) : RestApiDocumentationTest(accountancyProperties) {

    @Test
    fun `should get invoice details by id`() {

        // given
        transactionTemplate.execute { invoiceRepository.deleteAll() }

        // and
        val savedInvoice = transactionTemplate.execute {
            invoiceRepository.save(
                Invoice(
                    status = InvoiceStatus.PAID,
                    type = InvoiceType.OUTCOME,
                    orderNumber = 1L,
                    total = BigDecimal("90.00"),
                    productItems = setOf(
                        ProductItem(
                            productId = 1,
                            quantity = 3
                        )
                    )
                )
            )
        }.get()

        of("get-invoice-details-by-order-id").`when`()
            .pathParam("id", savedInvoice.orderNumber)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .get("http://${accountancyProperties.host}:$port/api/v1/accountancy/invoice/details/order-id/{id}")
            .then()
            .assertThat().statusCode(200)
            .assertThat().body("[0].productId", equalTo(1))
            .assertThat().body("[0].quantity", equalTo(3))
    }
}
