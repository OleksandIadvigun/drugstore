package sigma.software.leovegas.drugstore.accountancy.restdoc.invoice

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.matching.ContainsPattern
import java.math.BigDecimal
import org.hamcrest.Matchers.emptyString
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.accountancy.InvoiceRepository
import sigma.software.leovegas.drugstore.accountancy.api.CreateOutcomeInvoiceRequest
import sigma.software.leovegas.drugstore.accountancy.api.ItemDTO
import sigma.software.leovegas.drugstore.accountancy.client.AccountancyProperties
import sigma.software.leovegas.drugstore.accountancy.restdoc.RestApiDocumentationTest
import sigma.software.leovegas.drugstore.product.api.ProductDetailsResponse

@DisplayName("Create outcome invoice REST API Doc test")
class RestApiDocCreateOutcomeInvoiceTest @Autowired constructor(
    val objectMapper: ObjectMapper,
    @LocalServerPort val port: Int,
    val accountancyProperties: AccountancyProperties,
    val transactionalTemplate: TransactionTemplate,
    val invoiceRepository: InvoiceRepository
) : RestApiDocumentationTest(accountancyProperties) {

    @Test
    fun `should create invoice`() {

        // given
        transactionalTemplate.execute {
            invoiceRepository.deleteAll()
        }

        // and
        val invoiceRequest = listOf(
            ItemDTO(
                productId = 1L,
                quantity = 2,
            ),
            ItemDTO(
                productId = 2L,
                quantity = 2,
            )
        )

        val productsDetails = listOf(
            ProductDetailsResponse(
                id = 1L,
                name = "test1",
                price = BigDecimal("20.00"),
                quantity = 3,
            ),
            ProductDetailsResponse(
                id = 2L,
                name = "test2",
                price = BigDecimal("10.00"),
                quantity = 3,
            )
        )

        stubFor(
            get("/api/v1/products/details?ids=${invoiceRequest[0].productId}&ids=${invoiceRequest[1].productId}")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(
                    aResponse()
                        .withBody(
                            objectMapper
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(productsDetails)
                        )
                        .withStatus(HttpStatus.OK.value())
                )
        )

        val body = objectMapper
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(CreateOutcomeInvoiceRequest(invoiceRequest, 1))

        of("create-outcome-invoice").`when`()
            .body(body)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .post("http://${accountancyProperties.host}:$port/api/v1/accountancy/invoice/outcome")
            .then()
            .assertThat().statusCode(201)
            .assertThat().body("createdAt", not(emptyString()))
            .assertThat().body("status", equalTo("CREATED"))
            .assertThat().body("type", equalTo("OUTCOME"))
            .assertThat().body("total", equalTo(60.0F))
    }
}
