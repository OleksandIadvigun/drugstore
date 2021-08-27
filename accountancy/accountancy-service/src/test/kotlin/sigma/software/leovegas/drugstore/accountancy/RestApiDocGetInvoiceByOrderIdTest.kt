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

@DisplayName("Get invoice by order id REST API Doc test")
class RestApiDocGetInvoiceByOrderIdTest @Autowired constructor(
    @LocalServerPort val port: Int,
    val accountancyProperties: AccountancyProperties,
    val invoiceRepository: InvoiceRepository,
    val transactionTemplate: TransactionTemplate
) : RestApiDocumentationTest(accountancyProperties) {

    @Test
    fun `should get invoice by order id`() {

        // given
        transactionTemplate.execute {
            invoiceRepository.deleteAll()
        }

        // and
        val invoiceCreated = transactionTemplate.execute {
            invoiceRepository.save(
                Invoice(
                    orderId = 1,
                    status = InvoiceStatus.CREATED,
                    total = BigDecimal("90"),
                    productItems = setOf(
                        ProductItem(
                            priceItemId = 1L,
                            name = "test1",
                            price = BigDecimal("30"),
                            quantity = 3
                        )
                    )
                )
            )
        } ?: fail("result is expected")

        of("get-invoice-by-order-id").`when`()
            .pathParam("id", invoiceCreated.orderId)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .get("http://${accountancyProperties.host}:$port/api/v1/accountancy/invoice/order-id/{id}")
            .then()
            .assertThat().statusCode(200)
            .assertThat().body("orderId", equalTo(1))
            .assertThat().body("status", equalTo("CREATED"))
            .assertThat().body("total", equalTo(90.0F))
            .assertThat().body("productItems[0].priceItemId", equalTo(1))
            .assertThat().body("productItems[0].quantity", equalTo(3))
            .assertThat().body("productItems[0].price", equalTo(30.0F))
    }
}
