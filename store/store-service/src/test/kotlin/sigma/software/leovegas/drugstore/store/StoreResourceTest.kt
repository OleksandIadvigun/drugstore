package sigma.software.leovegas.drugstore.store

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.put
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import java.math.BigDecimal
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.api.protobuf.Proto
import sigma.software.leovegas.drugstore.api.toDecimalProto
import sigma.software.leovegas.drugstore.infrastructure.extensions.get
import sigma.software.leovegas.drugstore.infrastructure.extensions.respTypeRef
import sigma.software.leovegas.drugstore.infrastructure.extensions.withProtobufRequest
import sigma.software.leovegas.drugstore.infrastructure.extensions.withProtobufResponse
import sigma.software.leovegas.drugstore.product.api.DeliverProductsQuantityRequest
import sigma.software.leovegas.drugstore.store.api.TransferCertificateResponse
import sigma.software.leovegas.drugstore.store.api.TransferStatusDTO

@DisplayName("StoreResource test")
class StoreResourceTest @Autowired constructor(
    @LocalServerPort val port: Int,
    val restTemplate: TestRestTemplate,
    val transactionTemplate: TransactionTemplate,
    val storeRepository: StoreRepository,
    val storeProperties: StoreProperties
) : WireMockTest() {

    lateinit var baseUrl: String

    @BeforeEach
    fun setup() {
        baseUrl = "http://${storeProperties.host}:$port"
    }

    @Test
    fun `should get transfer certificates by order number`() {

        // given
        transactionTemplate.execute {
            storeRepository.deleteAllInBatch()
        }

        val orderNumber = "1"

        // and
        transactionTemplate.execute {
            storeRepository.save(
                TransferCertificate(
                    certificateNumber = "1",
                    orderNumber = orderNumber,
                    status = TransferStatus.RECEIVED,
                    comment = "RECEIVED"
                )
            )
        }.get()

        // when
        val response = restTemplate.exchange(
            "$baseUrl/api/v1/store/transfer-certificate/order/$orderNumber",
            HttpMethod.GET, null, respTypeRef<List<TransferCertificateResponse>>()
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        // and
        val body = response.body.get("body")
        assertThat(body).isNotNull
        assertThat(body).hasSize(1)
        assertThat(body[0].orderNumber).isEqualTo("1")
    }

    @Test
    fun `should get transfer certificates`() {

        // given
        transactionTemplate.execute {
            storeRepository.deleteAllInBatch()
        }

        // and
        transactionTemplate.execute {
            storeRepository.saveAll(
                listOf(
                    TransferCertificate(
                        certificateNumber = "1",
                        orderNumber = "1",
                        status = TransferStatus.RECEIVED,
                        comment = "RECEIVED"
                    ),
                    TransferCertificate(
                        certificateNumber = "2",
                        orderNumber = "2",
                        status = TransferStatus.DELIVERED,
                        comment = "DELIVERED"
                    )
                )
            )
        }

        // when
        val response = restTemplate.exchange(
            "$baseUrl/api/v1/store/transfer-certificate",
            HttpMethod.GET, null, respTypeRef<List<TransferCertificateResponse>>()
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        // and
        val body = response.body.get("body")
        assertThat(body).isNotNull
        assertThat(body).hasSize(2)
        assertThat(body[0].orderNumber).isEqualTo("1")
        assertThat(body[1].orderNumber).isEqualTo("2")
    }

    @Test
    fun `should receive products`() {

        // given
        transactionTemplate.execute {
            storeRepository.deleteAllInBatch()
        }

        // and
        val itemsList = listOf(
            Proto.Item.newBuilder().setProductNumber("1").setQuantity(2).build(),
            Proto.Item.newBuilder().setProductNumber("2").setQuantity(3).build(),
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
        val productRequest = Proto.ProductNumberList.newBuilder().addAllProductNumber(listOf("1", "2")).build()

        // and
        val productResponse = Proto.ReceiveProductResponse.newBuilder()
            .addAllProducts(
                listOf(
                    Proto.ReceiveProductItemDTO.newBuilder()
                        .setProductNumber("1")
                        .setStatus(Proto.ProductStatusDTO.RECEIVED).build(),
                    Proto.ReceiveProductItemDTO.newBuilder()
                        .setProductNumber("2")
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

        // when
        val response = restTemplate.exchange(
            "$baseUrl/api/v1/store/receive/$orderNumber",
            HttpMethod.PUT, null, respTypeRef<TransferCertificateResponse>()
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.ACCEPTED)

        // and
        val body = response.body.get("body")
        assertThat(body).isNotNull
        assertThat(body.orderNumber).isEqualTo("1")
        assertThat(body.status).isEqualTo(TransferStatusDTO.RECEIVED)
    }

    @Test
    fun `should deliver products`() {

        // given
        transactionTemplate.execute {
            storeRepository.deleteAllInBatch()
        }

        // and
        val itemsList = listOf(
            Proto.Item.newBuilder().setProductNumber("1").setQuantity(2).build(),
            Proto.Item.newBuilder().setProductNumber("2").setQuantity(3).build(),
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
                    Proto.Item.newBuilder().setProductNumber("2").setQuantity(3).build()
                )
            )
            .build()

        // and
        val productResponse = Proto.DeliverProductsDTO.newBuilder()
            .addAllItems(
                listOf(
                    Proto.Item.newBuilder().setProductNumber("1").setQuantity(5).build(),
                    Proto.Item.newBuilder().setProductNumber("2").setQuantity(10).build()
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
                .build(),
            Proto.ProductDetailsItem.newBuilder()
                .setName("test2").setProductNumber("2").setQuantity(20)
                .setPrice(BigDecimal("30.00").toDecimalProto())
                .build()
        )
        Proto.ProductDetailsResponse.newBuilder().addAllProducts(productsProto).build()

        // given
        stubFor(
            WireMock.get("/api/v1/products/details?productNumbers=1&productNumbers=2")
                .willReturn(
                    aResponse()
                        .withProtobufResponse {
                            Proto.ProductDetailsResponse.newBuilder().addAllProducts(productsProto).build()
                        }
                )
        )


        // when
        val response = restTemplate.exchange(
            "$baseUrl/api/v1/store/deliver/$orderNumber",
            HttpMethod.PUT, null, respTypeRef<TransferCertificateResponse>()
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.ACCEPTED)

        // and
        val body = response.body.get("body")
        assertThat(body).isNotNull
        assertThat(body).isNotNull
        assertThat(body.orderNumber).isEqualTo("1")
        assertThat(body.status).isEqualTo(TransferStatusDTO.DELIVERED)
    }

    @Test
    fun `should return products to reduce if products available`() {

        // given
        val products = listOf(
            DeliverProductsQuantityRequest(
                productNumber = "1",
                quantity = 2
            ),
            DeliverProductsQuantityRequest(
                productNumber = "2",
                quantity = 3
            )
        )

        // and
        val httpEntity = HttpEntity(products)

        // and
        val productsProto = listOf(
            Proto.ProductDetailsItem.newBuilder()
                .setName("test1").setProductNumber("1").setQuantity(10)
                .setPrice(BigDecimal("20.00").toDecimalProto())
                .build(),
            Proto.ProductDetailsItem.newBuilder()
                .setName("test2").setProductNumber("2").setQuantity(20)
                .setPrice(BigDecimal("30.00").toDecimalProto())
                .build()
        )
        Proto.ProductDetailsResponse.newBuilder().addAllProducts(productsProto).build()

        // given
        stubFor(
            WireMock.get("/api/v1/products/details?productNumbers=1&productNumbers=2")
                .willReturn(
                    aResponse()
                        .withProtobufResponse {
                            Proto.ProductDetailsResponse.newBuilder().addAllProducts(productsProto).build()
                        }
                )
        )

        // when
        val response = restTemplate.exchange(
            "$baseUrl/api/v1/store/availability",
            HttpMethod.PUT, httpEntity, respTypeRef<List<DeliverProductsQuantityRequest>>()
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.ACCEPTED)

        // and
        val body = response.body.get("body")
        assertThat(body).hasSize(2)
    }

    @Test
    fun `should return orderNumber if no transfer certificate found`() {

        // setup
        transactionTemplate.execute {
            storeRepository.deleteAll()
        }

        // given
        val orderNumber = "1b"

        // when
        val response = restTemplate.exchange(
            "$baseUrl/api/v1/store/check-transfer/$orderNumber",
            HttpMethod.GET, null, respTypeRef<Proto.CheckTransferResponse>()
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        // and
        val body = response.body.get()
        assertThat(body.orderNumber).isEqualTo(orderNumber)
    }
}
