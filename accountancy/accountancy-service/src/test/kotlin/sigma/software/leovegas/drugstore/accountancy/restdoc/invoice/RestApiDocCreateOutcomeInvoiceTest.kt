package sigma.software.leovegas.drugstore.accountancy.restdoc.invoice

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.google.protobuf.ByteString
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
import sigma.software.leovegas.drugstore.accountancy.api.CreateOutcomeInvoiceEvent
import sigma.software.leovegas.drugstore.accountancy.api.ItemDTO
import sigma.software.leovegas.drugstore.accountancy.client.AccountancyProperties
import sigma.software.leovegas.drugstore.infrastructure.RestApiDocumentationTest
import sigma.software.leovegas.drugstore.api.protobuf.Proto
import sigma.software.leovegas.drugstore.api.toDecimalProto
import sigma.software.leovegas.drugstore.extensions.withProtobufResponse

@DisplayName("Create outcome invoice REST API Doc test")
class RestApiDocCreateOutcomeInvoiceTest @Autowired constructor(
    val transactionalTemplate: TransactionTemplate,
    accountancyProperties: AccountancyProperties,
    val invoiceRepository: InvoiceRepository,
    val objectMapper: ObjectMapper,
    @LocalServerPort val port: Int,
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
                productNumber = "1",
                quantity = 2,
            )
        )

        // and
        val productsProto = listOf(
            Proto.ProductDetailsItem.newBuilder()
                .setName("test1").setProductNumber("1").setQuantity(3)
                .setPrice(BigDecimal("20.00").toDecimalProto())
                .build(),

            )

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

        // and
        val price = BigDecimal("40.00")
        val protoPrice = Proto.DecimalValue.newBuilder()
            .setPrecision(price.precision())
            .setScale(price.scale())
            .setValue(ByteString.copyFrom(price.unscaledValue().toByteArray()))
            .build()
        val responseExpected = Proto.ProductsPrice.newBuilder()
            .putItems("1", protoPrice)
            .build()

        // and
        stubFor(
            WireMock.get("/api/v1/products/1/price")
                .willReturn(
                    aResponse()
                        .withProtobufResponse { responseExpected }
                        .withStatus(HttpStatus.OK.value())
                )
        )

        val body = objectMapper
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(CreateOutcomeInvoiceEvent(invoiceRequest, "1"))

        of("create-outcome-invoice").`when`()
            .body(body)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .post("http://${accountancyProperties.host}:$port/api/v1/accountancy/invoice/outcome")
            .then()
            .assertThat().statusCode(201)
            .assertThat().body("amount", equalTo(160.0F))
            .assertThat().body("orderNumber", equalTo("1"))
    }
}
