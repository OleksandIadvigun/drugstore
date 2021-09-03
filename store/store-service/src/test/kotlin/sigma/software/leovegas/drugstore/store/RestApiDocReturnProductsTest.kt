package sigma.software.leovegas.drugstore.store

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.put
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.matching.ContainsPattern
import com.github.tomakehurst.wiremock.matching.EqualToPattern
import java.time.LocalDateTime
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.accountancy.api.InvoiceResponse
import sigma.software.leovegas.drugstore.accountancy.api.InvoiceStatusDTO
import sigma.software.leovegas.drugstore.accountancy.api.InvoiceTypeDTO
import sigma.software.leovegas.drugstore.accountancy.api.ProductItemDTO
import sigma.software.leovegas.drugstore.product.api.DeliverProductsQuantityRequest
import sigma.software.leovegas.drugstore.product.api.DeliverProductsResponse

@DisplayName("Return products REST API Doc test")
class RestApiDocReturnProductsTest @Autowired constructor(
    val objectMapper: ObjectMapper,
    @LocalServerPort val port: Int,
    val storeProperties: StoreProperties,
    val storeRepository: StoreRepository,
    val transactionTemplate: TransactionTemplate
) : RestApiDocumentationTest(storeProperties) {

    @Test
    fun `should return products`() {

        transactionTemplate.execute {
            storeRepository.deleteAllInBatch()
        }

        val accountancyResponse = InvoiceResponse(
            id = 1,
            orderId = 1,
            type = InvoiceTypeDTO.INCOME,
            status = InvoiceStatusDTO.RECEIVED,
            productItems = setOf(
                ProductItemDTO(productId = 1, quantity = 2)
            )
        )

        // and
        stubFor(
            get("/api/v1/accountancy/invoice/1")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(
                    aResponse()
                        .withBody(
                            objectMapper
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(accountancyResponse)
                        )
                )
        )

        //and
        val productDetailsResponse = listOf(
            DeliverProductsResponse(
                id = 1L,
                quantity = 5,
                updatedAt = LocalDateTime.now()
            )
        )

        //and
        stubFor(
            get("/api/v1/products/details?ids=1")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(
                    aResponse()
                        .withBody(
                            objectMapper
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(productDetailsResponse)
                        )
                        .withStatus(HttpStatus.ACCEPTED.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                )
        )

        // and
        val productRequest = listOf(
            DeliverProductsQuantityRequest(
                id = 1,
                quantity = 2
            )
        )

        //and
        val productResponse = listOf(
            DeliverProductsResponse(
                id = 1L,
                quantity = 5,
                updatedAt = LocalDateTime.now()
            )
        )

        //and
        stubFor(
            put("/api/v1/products/deliver")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
                .withRequestBody(
                    EqualToPattern(
                        objectMapper
                            .writerWithDefaultPrettyPrinter()
                            .writeValueAsString(productRequest)
                    )
                )
                .willReturn(
                    aResponse()
                        .withBody(
                            objectMapper
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(productResponse)
                        )
                        .withStatus(HttpStatus.ACCEPTED.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                )
        )

        of("return-quantity")
            .`when`()
            .body(1)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .put("http://${storeProperties.host}:$port/api/v1/store/return")
            .then()
            .assertThat().statusCode(202)
            .assertThat().body("invoiceId", equalTo(1))
    }
}
