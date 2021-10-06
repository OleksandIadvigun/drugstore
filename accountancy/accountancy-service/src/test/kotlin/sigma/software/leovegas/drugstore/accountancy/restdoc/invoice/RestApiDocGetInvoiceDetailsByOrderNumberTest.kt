package sigma.software.leovegas.drugstore.accountancy.restdoc.invoice

import io.restassured.RestAssured
import java.math.BigDecimal
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.accountancy.Invoice
import sigma.software.leovegas.drugstore.accountancy.InvoiceRepository
import sigma.software.leovegas.drugstore.accountancy.InvoiceStatus
import sigma.software.leovegas.drugstore.accountancy.InvoiceType
import sigma.software.leovegas.drugstore.accountancy.ProductItem
import sigma.software.leovegas.drugstore.accountancy.client.AccountancyProperties
import sigma.software.leovegas.drugstore.accountancy.restdoc.RestApiDocumentationTest
import sigma.software.leovegas.drugstore.extensions.get

@DisplayName("Get invoice details by order number REST API Doc test")
class RestApiDocGetInvoiceDetailsByOrderNumberTest @Autowired constructor(
    val accountancyProperties: AccountancyProperties,
    val transactionTemplate: TransactionTemplate,
    val invoiceRepository: InvoiceRepository,
    @LocalServerPort val port: Int,
) : RestApiDocumentationTest(accountancyProperties) {

    @Disabled
    @Test
    fun `should get invoice details by order number`() {

        // given
        transactionTemplate.execute { invoiceRepository.deleteAll() }

        // and
        val savedInvoice = transactionTemplate.execute {
            invoiceRepository.save(
                Invoice(
                    status = InvoiceStatus.PAID,
                    type = InvoiceType.OUTCOME,
                    invoiceNumber = "1",
                    orderNumber = "1",
                    total = BigDecimal("90.00"),
                    productItems = setOf(
                        ProductItem(
                            productNumber = "1",
                            quantity = 3
                        )
                    )
                )
            )
        }.get()

        RestAssured.registerParser(
            "application/x-protobuf",
            io.restassured.parsing.Parser.fromContentType("x-protobuf")
        )
        of("get-invoice-details-by-order-number").`when`()
            .pathParam("orderNumber", savedInvoice.orderNumber)
            .contentType("application/x-protobuf")
            .get("http://${accountancyProperties.host}:$port/api/v1/accountancy/invoice/details/order-number/{orderNumber}")
            .then()
            .assertThat().statusCode(200)
            .assertThat().body("itemsList[0].productNumber", equalTo("1"))
            .assertThat().body("itemsList[0].quantity", equalTo(3))
    }
}
