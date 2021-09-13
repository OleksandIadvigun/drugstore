package sigma.software.leovegas.drugstore.accountancy.restdoc.invoice

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.matching.ContainsPattern
import com.github.tomakehurst.wiremock.matching.EqualToPattern
import java.math.BigDecimal
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.accountancy.InvoiceRepository
import sigma.software.leovegas.drugstore.accountancy.api.CreateIncomeInvoiceRequest
import sigma.software.leovegas.drugstore.accountancy.api.ProductItemDtoRequest
import sigma.software.leovegas.drugstore.accountancy.client.AccountancyProperties
import sigma.software.leovegas.drugstore.accountancy.restdoc.RestApiDocumentationTest
import sigma.software.leovegas.drugstore.product.api.CreateProductRequest
import sigma.software.leovegas.drugstore.product.api.ProductDetailsResponse

@DisplayName("Create income invoice REST API Doc test")
class RestApiDocCreateIncomeInvoiceTest @Autowired constructor(
    val objectMapper: ObjectMapper,
    @LocalServerPort val port: Int,
    val accountancyProperties: AccountancyProperties,
    val transactionalTemplate: TransactionTemplate,
    val invoiceRepository: InvoiceRepository
) : RestApiDocumentationTest(accountancyProperties) {

    @Test
    fun `should create income invoice`() {

        // given
        transactionalTemplate.execute { invoiceRepository.deleteAll() }

        // and
        val invoiceRequest = CreateIncomeInvoiceRequest(
            productItems = listOf(
                ProductItemDtoRequest(
                    name = "test1",
                    quantity = 1,
                    price = BigDecimal("20.00")
                ),
                ProductItemDtoRequest(
                    name = "test2",
                    quantity = 2,
                    price = BigDecimal("20.00")
                )
            )
        )

        // and
        val productsToCreate = listOf(
            CreateProductRequest(
                name = "test1",
                quantity = 1,
                price = BigDecimal("20.00")
            ),
            CreateProductRequest(
                name = "test2",
                quantity = 2,
                price = BigDecimal("20.00")
            )
        )


        val productsDetails = listOf(
            ProductDetailsResponse(
                productNumber = 1L,
                name = "test1",
                price = BigDecimal("20.00"),
                quantity = 1,
            ),
            ProductDetailsResponse(
                productNumber = 2L,
                name = "test2",
                price = BigDecimal("20.00"),
                quantity = 2,
            )
        )

        stubFor(
            post("/api/v1/products")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
                .withRequestBody(
                    EqualToPattern(
                        objectMapper
                            .writerWithDefaultPrettyPrinter()
                            .writeValueAsString(productsToCreate)
                    )
                )
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
            .writeValueAsString(invoiceRequest)

        of("create-income-invoice").`when`()
            .body(body)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .post("http://${accountancyProperties.host}:$port/api/v1/accountancy/invoice/income")
            .then()
            .assertThat().statusCode(201)
            .assertThat().body("amount", equalTo(60.0F))
    }
}
