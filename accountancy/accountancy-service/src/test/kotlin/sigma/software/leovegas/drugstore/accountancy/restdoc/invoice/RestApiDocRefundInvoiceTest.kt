package sigma.software.leovegas.drugstore.accountancy.restdoc.invoice

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import java.math.BigDecimal
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.accountancy.Invoice
import sigma.software.leovegas.drugstore.accountancy.InvoiceRepository
import sigma.software.leovegas.drugstore.accountancy.InvoiceStatus
import sigma.software.leovegas.drugstore.accountancy.ProductItem
import sigma.software.leovegas.drugstore.accountancy.client.AccountancyProperties
import sigma.software.leovegas.drugstore.accountancy.restdoc.RestApiDocumentationTest
import sigma.software.leovegas.drugstore.api.protobuf.Proto
import sigma.software.leovegas.drugstore.extensions.get
import sigma.software.leovegas.drugstore.extensions.withProtobufResponse

@DisplayName("Refund invoice REST API Doc test")
class RestApiDocRefundInvoiceTest @Autowired constructor(
    @LocalServerPort val port: Int,
    val accountancyProperties: AccountancyProperties,
    val transactionalTemplate: TransactionTemplate,
    val invoiceRepository: InvoiceRepository
) : RestApiDocumentationTest(accountancyProperties) {

    @Test
    fun `should refund invoice`() {

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
                    status = InvoiceStatus.PAID,
                    productItems = setOf(
                        ProductItem(
                            productNumber = "1",
                            name = "test",
                            price = BigDecimal("30"),
                            quantity = 3
                        )
                    )
                )
            )
        }.get()

        val responseExpected =
            Proto.CheckTransferResponse.newBuilder().setOrderNumber(savedInvoice.orderNumber)
                .setComment("Not delivered").build()

        stubFor(
            WireMock.get("/api/v1/store/check-transfer/${savedInvoice.orderNumber}")
                .willReturn(
                    aResponse()
                        .withProtobufResponse { responseExpected }
                        .withStatus(HttpStatus.OK.value())
                )
        )

        of("refund-invoice").`when`()
            .pathParam("orderNumber", savedInvoice.orderNumber)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .put("http://${accountancyProperties.host}:$port/api/v1/accountancy/invoice/refund/{orderNumber}")
            .then()
            .assertThat().statusCode(202)
            .assertThat().body("orderNumber", equalTo("1"))
            .assertThat().body("amount", equalTo(90.0F))
    }
}
