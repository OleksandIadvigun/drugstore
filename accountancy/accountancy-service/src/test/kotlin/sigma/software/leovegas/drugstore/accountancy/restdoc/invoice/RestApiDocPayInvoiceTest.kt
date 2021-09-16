package sigma.software.leovegas.drugstore.accountancy.restdoc.invoice

import com.fasterxml.jackson.databind.ObjectMapper
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
import sigma.software.leovegas.drugstore.accountancy.ProductItem
import sigma.software.leovegas.drugstore.accountancy.client.AccountancyProperties
import sigma.software.leovegas.drugstore.accountancy.restdoc.RestApiDocumentationTest
import sigma.software.leovegas.drugstore.extensions.get

@DisplayName("Pay invoice REST API Doc test")
class RestApiDocPayInvoiceTest @Autowired constructor(
    val objectMapper: ObjectMapper,
    @LocalServerPort val port: Int,
    val accountancyProperties: AccountancyProperties,
    val transactionalTemplate: TransactionTemplate,
    val invoiceRepository: InvoiceRepository
) : RestApiDocumentationTest(accountancyProperties) {

    @Test
    fun `should pay invoice`() {

        // given
        transactionalTemplate.execute {
            invoiceRepository.deleteAll()
        }

        // and
        val savedInvoice = transactionalTemplate.execute {
            invoiceRepository.save(
                Invoice(
                    invoiceNumber = "1",
                    orderNumber = "1",
                    total = BigDecimal("90.00"),
                    status = InvoiceStatus.CREATED,
                    productItems = setOf(
                        ProductItem(
                            productNumber = "1",
                            name = "test",
                            price = BigDecimal("30"),
                            quantity = 3
                        )
                    ),
                )
            )
        }.get()

        val body = objectMapper
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(BigDecimal("90.00"))

        of("pay-invoice").`when`()
            .pathParam("id", savedInvoice.orderNumber)
            .body(body)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .put("http://${accountancyProperties.host}:$port/api/v1/accountancy/invoice/pay/{id}")
            .then()
            .assertThat().statusCode(202)
            .assertThat().body("orderNumber", equalTo("1"))
            .assertThat().body("amount", equalTo(90.0F))
    }
}
