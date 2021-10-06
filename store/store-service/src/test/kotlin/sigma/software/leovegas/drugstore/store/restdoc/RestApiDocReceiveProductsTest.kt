package sigma.software.leovegas.drugstore.store.restdoc

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.put
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.api.protobuf.Proto
import sigma.software.leovegas.drugstore.infrastructure.extensions.withProtobufRequest
import sigma.software.leovegas.drugstore.infrastructure.extensions.withProtobufResponse
import sigma.software.leovegas.drugstore.store.StoreProperties
import sigma.software.leovegas.drugstore.store.StoreRepository

@DisplayName("Receive products REST API Doc test")
class RestApiDocReceiveProductsTest @Autowired constructor(
    val transactionTemplate: TransactionTemplate,
    val storeProperties: StoreProperties,
    val storeRepository: StoreRepository,
    @LocalServerPort val port: Int,
) : RestApiDocumentationTest(storeProperties) {

    @Test
    fun `should receive products quantity`() {

        // given
        transactionTemplate.execute {
            storeRepository.deleteAllInBatch()
        }

        // and
        val itemsList = listOf(
            Proto.Item.newBuilder().setProductNumber("1").setQuantity(2).build(),
        )
        val invoiceDetailsProto = Proto.InvoiceDetails.newBuilder().addAllItems(itemsList).build()

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
        val productRequest = Proto.ProductNumberList.newBuilder().addAllProductNumber(listOf("1")).build()

        // and
        val productResponse = Proto.ReceiveProductResponse.newBuilder()
            .addAllProducts(
                listOf(
                    Proto.ReceiveProductItemDTO.newBuilder()
                        .setProductNumber("1")
                        .setStatus(Proto.ProductStatusDTO.RECEIVED).build()
                )
            )
            .build()

        //and
        stubFor(
            put("/api/v1/products/receive")
                .withProtobufRequest { productRequest }
                .willReturn(
                    aResponse()
                        .withProtobufResponse { productResponse }
                        .withStatus(HttpStatus.ACCEPTED.value())
                )
        )

        of("receive-products")
            .`when`()
            .body(1)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .put("http://${storeProperties.host}:$port/api/v1/store/receive/$orderNumber")
            .then()
            .assertThat().statusCode(202)
            .assertThat().body("orderNumber", equalTo("1"))
    }
}
