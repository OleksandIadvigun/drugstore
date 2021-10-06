package sigma.software.leovegas.drugstore.store

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.put
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import java.math.BigDecimal
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.api.protobuf.Proto
import sigma.software.leovegas.drugstore.api.toDecimalProto
import sigma.software.leovegas.drugstore.infrastructure.extensions.get
import sigma.software.leovegas.drugstore.infrastructure.extensions.withProtobufRequest
import sigma.software.leovegas.drugstore.infrastructure.extensions.withProtobufResponse
import sigma.software.leovegas.drugstore.product.api.DeliverProductsQuantityRequest
import sigma.software.leovegas.drugstore.store.api.TransferCertificateRequest
import sigma.software.leovegas.drugstore.store.api.TransferStatusDTO

@SpringBootTest
@AutoConfigureTestDatabase
@DisplayName("Store Service test")
class StoreServiceTest @Autowired constructor(
    val transactionTemplate: TransactionTemplate,
    val storeRepository: StoreRepository,
    val storeService: StoreService,
    val objectMapper: ObjectMapper,
) : WireMockTest() {

    @Test
    fun `should create transfer certificate`() {

        // given
        transactionTemplate.execute {
            storeRepository.deleteAllInBatch()
        }

        // and
        val transferCertificateRequest = TransferCertificateRequest(
            orderNumber = "1",
            status = TransferStatusDTO.RECEIVED,
            comment = "RECEIVED"
        )

        // when
        val created = transactionTemplate.execute {
            storeService.createTransferCertificate(transferCertificateRequest)
        }.get()

        // then
        assertThat(created.certificateNumber).isNotNull
        assertThat(created.orderNumber).isEqualTo("1")
        assertThat(created.certificateNumber).isNotEqualTo("undefined")
        assertThat(created.status).isEqualTo(TransferStatusDTO.RECEIVED)
        assertThat(created.comment).isEqualTo("RECEIVED")

    }

    @Test
    fun `should get store transfer certificates by order number`() {

        // given
        transactionTemplate.execute {
            storeRepository.deleteAllInBatch()
        }

        // and
        val created = transactionTemplate.execute {
            storeRepository.save(
                TransferCertificate(
                    certificateNumber = "1",
                    orderNumber = "1",
                    status = TransferStatus.RECEIVED,
                    comment = "RECEIVED"
                )
            )
        }.get()

        // when
        val actual = transactionTemplate.execute {
            storeService.getTransferCertificatesByOrderNumber(created.orderNumber)
        }.get()

        // then
        assertThat(actual[0].orderNumber).isEqualTo(created.orderNumber)
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
        val actual = transactionTemplate.execute {
            storeService.getTransferCertificates(page = 0, size = 5)
        }.get()

        // then
        assertThat(actual).hasSize(2)
        assertThat(actual[0].status).isEqualTo(TransferStatusDTO.RECEIVED)
        assertThat(actual[0].orderNumber).isEqualTo("1")
        assertThat(actual[1].status).isEqualTo(TransferStatusDTO.DELIVERED)
        assertThat(actual[1].orderNumber).isEqualTo("2")
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
        val transferCertificate = transactionTemplate.execute {
            storeService.deliverProducts(orderNumber)
        }.get()

        // then
        assertThat(transferCertificate.certificateNumber).isNotNull
        assertThat(transferCertificate.orderNumber).isEqualTo(orderNumber)
        assertThat(transferCertificate.status).isEqualTo(TransferStatusDTO.DELIVERED)
    }

    @Test
    fun `should not deliver product if accountancy server not available`() {

        // setup
        transactionTemplate.execute {
            storeRepository.deleteAll()
        }

        // given
        val orderId = "1"

        // when
        val exception = assertThrows<AccountancyServerResponseException> {
            storeService.deliverProducts(orderId)
        }

        // then
        assertThat(exception.message).startsWith("Ups... some problems in accountancy service")
    }

    @Test
    fun `should not deliver products if product server not available`() {

        // given
        transactionTemplate.execute {
            storeRepository.deleteAllInBatch()
        }

        // and
        val itemsList = listOf(
            Proto.Item.newBuilder().setProductNumber("3").setQuantity(2).build(),
            Proto.Item.newBuilder().setProductNumber("4").setQuantity(3).build(),
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
        val exception = assertThrows<ProductServerResponseException> {
            storeService.deliverProducts(orderNumber)
        }

        // then
        assertThat(exception.message).startsWith("Ups... some problems in product service.")
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
        val transferCertificate = transactionTemplate.execute {
            storeService.receiveProduct(orderNumber)
        }.get()

        // then
        assertThat(transferCertificate.certificateNumber).isNotNull
        assertThat(transferCertificate.orderNumber).isEqualTo(orderNumber)
        assertThat(transferCertificate.status).isEqualTo(TransferStatusDTO.RECEIVED)
    }

    @Test
    fun `should not receive product if accountancy server not available`() {

        // given
        transactionTemplate.execute {
            storeRepository.deleteAll()
        }

        // and
        val orderId = "3"

        // when
        val exception = assertThrows<AccountancyServerResponseException> {
            storeService.receiveProduct(orderId)
        }

        // then
        assertThat(exception.message).startsWith("Ups... some problems in accountancy service.")
    }

    @Test
    fun `should not receive products if product server not available`() {

        // given
        transactionTemplate.execute {
            storeRepository.deleteAllInBatch()
        }

        // and
        val itemsList = listOf(
            Proto.Item.newBuilder().setProductNumber("5").setQuantity(2).build(),
            Proto.Item.newBuilder().setProductNumber("6").setQuantity(3).build(),
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

        // when
        val exception = assertThrows<ProductServerResponseException> {
            storeService.receiveProduct(orderNumber)
        }

        // then
        assertThat(exception.message).startsWith("Ups... some problems in product service.")

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
        val availability = transactionTemplate.execute {
            storeService.checkAvailability(products)
        }

        assertThat(availability).hasSize(2)
    }

    @Test
    fun `should not return products to reduce if products unavailable`() {

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
        val productsProto = listOf(
            Proto.ProductDetailsItem.newBuilder()
                .setName("test1").setProductNumber("1").setQuantity(0)
                .setPrice(BigDecimal("20.00").toDecimalProto())
                .build(),
            Proto.ProductDetailsItem.newBuilder()
                .setName("test2").setProductNumber("2").setQuantity(0)
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
        val exception = assertThrows<InsufficientAmountOfProductException> {
            storeService.checkAvailability(products)
        }
        assertThat(exception.message).contains("Insufficient amount product with id =")
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
        val actual = storeService.checkTransfer(orderNumber)

        // then
        assertThat(actual.orderNumber).isEqualTo(orderNumber)
    }

    @Test
    fun `should not return orderNumber if  transfer certificate was found`() {

        // setup
        transactionTemplate.execute {
            storeRepository.deleteAll()
        }

        // given
        val orderNumber = "1B"

        // and
        transactionTemplate.execute {
            storeRepository.save(
                TransferCertificate(
                    orderNumber = orderNumber, status = TransferStatus.DELIVERED
                )
            )
        }

        // when
        val exception = assertThrows<ProductsAlreadyDelivered> {
            storeService.checkTransfer(orderNumber)
        }
        assertThat(exception.message).contains("Products from order($orderNumber) already delivered")
    }
}
