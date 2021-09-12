package sigma.software.leovegas.drugstore.store.restdoc

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock
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
import sigma.software.leovegas.drugstore.accountancy.api.ItemDTO
import sigma.software.leovegas.drugstore.api.protobuf.AccountancyProto
import sigma.software.leovegas.drugstore.infrastructure.extensions.withProtobufResponse
import sigma.software.leovegas.drugstore.product.api.DeliverProductsQuantityRequest
import sigma.software.leovegas.drugstore.product.api.DeliverProductsResponse
import sigma.software.leovegas.drugstore.product.api.ProductDetailsResponse
import sigma.software.leovegas.drugstore.store.StoreProperties
import sigma.software.leovegas.drugstore.store.StoreRepository

@DisplayName("Deliver products REST API Doc test")
class RestApiDocDeliverProductsTest @Autowired constructor(
    val objectMapper: ObjectMapper,
    @LocalServerPort val port: Int,
    val storeProperties: StoreProperties,
    val storeRepository: StoreRepository,
    val transactionTemplate: TransactionTemplate
) : RestApiDocumentationTest(storeProperties) {

    @Test
    fun `should deliver products`() {

        // given
        transactionTemplate.execute {
            storeRepository.deleteAllInBatch()
        }

        // and
        val itemsList = listOf(
            AccountancyProto.Item.newBuilder().setProductNumber( "1").setQuantity(2).build(),
        )
        val invoiceDetailsProto = AccountancyProto.InvoiceDetails.newBuilder().addAllItems(itemsList).build()

        // and
        val orderNumber = "1"

        // and
        stubFor(
            WireMock.get("/api/v1/accountancy/invoice/details/order-number/$orderNumber")
                .willReturn(
                    aResponse()
                        .withProtobufResponse { invoiceDetailsProto }
                )
        )


        // and
        val productRequest = listOf(
            DeliverProductsQuantityRequest(
                productNumber = "1",
                quantity = 2
            )
        )

        //and
        val productResponse = listOf(
            DeliverProductsResponse(
                productNumber = "1",
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

        // and
        val productDetailsResponse = listOf(
            ProductDetailsResponse(
                productNumber = "1",
                quantity = 10
            ),
        )

        // and
        stubFor(
            get("/api/v1/products/details?productNumbers=${productDetailsResponse[0].productNumber}")
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

        of("deliver-products").`when`()
            .body(1)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .put("http://${storeProperties.host}:$port/api/v1/store/deliver")
            .then()
            .assertThat().statusCode(202)
            .assertThat().body("orderNumber", equalTo("1"))
    }
}
