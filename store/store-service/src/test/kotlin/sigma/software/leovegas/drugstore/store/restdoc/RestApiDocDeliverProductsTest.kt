package sigma.software.leovegas.drugstore.store.restdoc

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.put
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
import sigma.software.leovegas.drugstore.api.protobuf.Proto
import sigma.software.leovegas.drugstore.api.toDecimalProto
import sigma.software.leovegas.drugstore.infrastructure.extensions.withProtobufRequest
import sigma.software.leovegas.drugstore.infrastructure.extensions.withProtobufResponse
import sigma.software.leovegas.drugstore.store.StoreProperties
import sigma.software.leovegas.drugstore.store.StoreRepository

@DisplayName("Deliver products REST API Doc test")
class RestApiDocDeliverProductsTest @Autowired constructor(
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
        val productRequest = Proto.DeliverProductsDTO.newBuilder()
            .addAllItems(
                listOf(
                    Proto.Item.newBuilder().setProductNumber("1").setQuantity(2).build(),
                )
            )
            .build()

        // and
        val productResponse = Proto.DeliverProductsDTO.newBuilder()
            .addAllItems(
                listOf(
                    Proto.Item.newBuilder().setProductNumber("1").setQuantity(5).build(),
                )
            )
            .build()

        // and
        stubFor(
            put("/api/v1/products/deliver")
                .withProtobufRequest { productRequest }
                .willReturn(
                    aResponse()
                        .withProtobufResponse { productResponse }
                        .withStatus(HttpStatus.ACCEPTED.value())
                )
        )

        // and
        val productsProto = listOf(
            Proto.ProductDetailsItem.newBuilder()
                .setName("test1").setProductNumber("1").setQuantity(10)
                .setPrice(BigDecimal("20.00").toDecimalProto())
                .build()
        )
        Proto.ProductDetailsResponse.newBuilder().addAllProducts(productsProto).build()

        // given
        stubFor(
            WireMock.get("/api/v1/products/details?productNumbers=1")
                .willReturn(
                    aResponse()
                        .withProtobufResponse {
                            Proto.ProductDetailsResponse.newBuilder().addAllProducts(productsProto).build()
                        }
                )
        )

        of("deliver-products").`when`()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .put("http://${storeProperties.host}:$port/api/v1/store/deliver/$orderNumber")
            .then()
            .assertThat().statusCode(202)
            .assertThat().body("orderNumber", equalTo("1"))
    }
}
