package sigma.software.leovegas.drugstore.accountancy.restdoc.invoice

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.put
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.matching.ContainsPattern
import com.github.tomakehurst.wiremock.matching.EqualToPattern
import java.math.BigDecimal
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
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
import sigma.software.leovegas.drugstore.order.api.OrderResponse
import sigma.software.leovegas.drugstore.order.api.OrderStatusDTO

@DisplayName("Refund invoice REST API Doc test")
class RestApiDocRefundInvoiceTest @Autowired constructor(
    val objectMapper: ObjectMapper,
    @LocalServerPort val port: Int,
    val accountancyProperties: AccountancyProperties,
    val transactionalTemplate: TransactionTemplate,
    val invoiceRepository: InvoiceRepository
) : RestApiDocumentationTest(accountancyProperties) {

    @Test
    fun `should refund invoice`() {

        // given
        val savedInvoice = transactionalTemplate.execute {
            invoiceRepository.save(
                Invoice(
                    orderId = 1L,
                    total = BigDecimal("90.00"),
                    status = InvoiceStatus.PAID,
                    productItems = setOf(
                        ProductItem(
                            productId = 1L,
                            name = "test",
                            price = BigDecimal("30"),
                            quantity = 3
                        )
                    )
                )
            )
        } ?: fail("result is expected")

        stubFor(
            put("/api/v1/orders/change-status/${savedInvoice.orderId}")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
                .withRequestBody(
                    EqualToPattern(
                        objectMapper
                            .writerWithDefaultPrettyPrinter()
                            .writeValueAsString(OrderStatusDTO.REFUND)
                    )
                )
                .willReturn(
                    aResponse()
                        .withBody(
                            objectMapper
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(OrderResponse(orderStatus = OrderStatusDTO.REFUND))
                        )
                        .withStatus(HttpStatus.OK.value())
                )
        )

        of("refund-invoice").`when`()
            .pathParam("id", savedInvoice.id)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .put("http://${accountancyProperties.host}:$port/api/v1/accountancy/invoice/refund/{id}")
            .then()
            .assertThat().statusCode(202)
            .assertThat().body("status", equalTo("REFUND"))
    }
}
