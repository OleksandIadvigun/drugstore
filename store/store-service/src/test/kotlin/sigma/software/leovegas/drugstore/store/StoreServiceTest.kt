package sigma.software.leovegas.drugstore.store

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.put
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.matching.ContainsPattern
import com.github.tomakehurst.wiremock.matching.EqualToPattern
import java.math.BigDecimal
import java.time.LocalDateTime
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.accountancy.api.InvoiceResponse
import sigma.software.leovegas.drugstore.accountancy.api.InvoiceStatusDTO
import sigma.software.leovegas.drugstore.accountancy.api.InvoiceTypeDTO
import sigma.software.leovegas.drugstore.accountancy.api.ProductItemDTO
import sigma.software.leovegas.drugstore.product.api.DeliverProductsQuantityRequest
import sigma.software.leovegas.drugstore.product.api.DeliverProductsResponse
import sigma.software.leovegas.drugstore.product.api.ProductDetailsResponse
import sigma.software.leovegas.drugstore.product.api.ProductStatusDTO
import sigma.software.leovegas.drugstore.product.api.ReceiveProductResponse
import sigma.software.leovegas.drugstore.store.api.TransferCertificateRequest
import sigma.software.leovegas.drugstore.store.api.TransferStatusDTO

@SpringBootTest
@AutoConfigureTestDatabase
@DisplayName("Store Service test")
class StoreServiceTest @Autowired constructor(
    val storeRepository: StoreRepository,
    val transactionTemplate: TransactionTemplate,
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
            invoiceId = 1,
            status = TransferStatusDTO.RECEIVED,
            comment = "RECEIVED"
        )

        // when
        val created = transactionTemplate.execute {
            storeService.createTransferCertificate(transferCertificateRequest)
        } ?: fail("result expected")

        // then
        assertThat(created.id).isNotNull
        assertThat(created.invoiceId).isEqualTo(1)
        assertThat(created.status).isEqualTo(TransferStatusDTO.RECEIVED)
        assertThat(created.comment).isEqualTo("RECEIVED")

    }

    @Test
    fun `should get store transfer certificates by invoice id`() {

        // given
        transactionTemplate.execute {
            storeRepository.deleteAllInBatch()
        }

        // and
        val created = transactionTemplate.execute {
            storeRepository.save(
                TransferCertificate(
                    invoiceId = 1,
                    status = TransferStatus.RECEIVED,
                    comment = "RECEIVED"
                )
            )
        } ?: fail("result is expected")

        // when
        val actual = transactionTemplate.execute {
            storeService.getTransferCertificatesByInvoiceId(created.invoiceId)
        } ?: fail("result is expected")

        // then
        assertThat(actual[0].invoiceId).isEqualTo(created.invoiceId)
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
                        invoiceId = 1,
                        status = TransferStatus.RECEIVED,
                        comment = "RECEIVED"
                    ),
                    TransferCertificate(
                        invoiceId = 2,
                        status = TransferStatus.DELIVERED,
                        comment = "DELIVERED"
                    )
                )
            )
        }

        // when
        val actual = transactionTemplate.execute {
            storeService.getTransferCertificates()
        } ?: fail("result is expected")

        // then
        assertThat(actual).hasSize(2)
        assertThat(actual[0].status).isEqualTo(TransferStatusDTO.RECEIVED)
        assertThat(actual[0].invoiceId).isEqualTo(1)
        assertThat(actual[1].status).isEqualTo(TransferStatusDTO.DELIVERED)
        assertThat(actual[1].invoiceId).isEqualTo(2)
    }

    @Test
    fun `should deliver products`() {

        // given
        transactionTemplate.execute {
            storeRepository.deleteAllInBatch()
        }

        // and
        val accountancyResponse = InvoiceResponse(
            id = 1,
            orderId = 1,
            type = InvoiceTypeDTO.OUTCOME,
            total = BigDecimal("90.00"),
            status = InvoiceStatusDTO.PAID,
            productItems = setOf(
                ProductItemDTO(productId = 1, quantity = 2)
            )
        )

        // and
        stubFor(
            get("/api/v1/accountancy/invoice/order-id/1")
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

        // and
        val productDetailsResponse = listOf(
            ProductDetailsResponse(
                id = 1,
                quantity = 10
            ),
        )

        // and
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

        // when
        val transferCertificate = transactionTemplate.execute {
            storeService.deliverProducts(1)
        } ?: fail("result is expected")

        // then
        assertThat(transferCertificate.id).isNotNull
        assertThat(transferCertificate.invoiceId).isEqualTo(accountancyResponse.id)
        assertThat(transferCertificate.status).isEqualTo(TransferStatusDTO.DELIVERED)
    }

    @Test
    fun `should not deliver product by not outcome invoice`() {

        //given
        transactionTemplate.execute {
            storeRepository.deleteAllInBatch()
        }

        // and
        val accountancyResponse = InvoiceResponse(
            id = 1,
            orderId = 1,
            type = InvoiceTypeDTO.INCOME,
            total = BigDecimal("90.00"),
            status = InvoiceStatusDTO.PAID,
        )

        // and
        stubFor(
            get("/api/v1/accountancy/invoice/order-id/1")
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

        // when
        val exception = assertThrows<IncorrectTypeOfInvoice> {
            storeService.deliverProducts(1)
        }

        // then
        assertThat(exception.message).contains("Invoice type should be outcome")
    }

    @Test
    fun `should not deliver products if status not paid`() {

        //given
        transactionTemplate.execute {
            storeRepository.deleteAllInBatch()
        }
        val accountancyResponse = InvoiceResponse(
            id = 1,
            orderId = 1,
            type = InvoiceTypeDTO.OUTCOME,
            status = InvoiceStatusDTO.CREATED,
        )

        // and
        stubFor(
            get("/api/v1/accountancy/invoice/order-id/1")
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

        // when
        val exception = assertThrows<IncorrectStatusOfInvoice> {
            storeService.deliverProducts(1)
        }

        // then
        assertThat(exception.message).contains("Invoice is not paid")

    }

    @Test
    fun `should receive products`() {

        //given
        transactionTemplate.execute {
            storeRepository.deleteAllInBatch()
        }

        // and
        val accountancyResponse = InvoiceResponse(
            id = 1,
            orderId = 1,
            type = InvoiceTypeDTO.INCOME,
            status = InvoiceStatusDTO.PAID,
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

        // and
        val productRequest = listOf(1)

        // and
        val productResponse = listOf(
            ReceiveProductResponse(
                id = 1,
                status = ProductStatusDTO.RECEIVED,
            )
        )

        // and
        stubFor(
            put("/api/v1/products/receive")
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

        // when
        val transferCertificate = transactionTemplate.execute {
            storeService.receiveProduct(1)
        } ?: fail("result is expected")

        // then
        assertThat(transferCertificate.id).isNotNull
        assertThat(transferCertificate.status).isEqualTo(TransferStatusDTO.RECEIVED)
        assertThat(transferCertificate.invoiceId).isEqualTo(1)
    }

    @Test
    fun `should not receive product by not income invoice`() {

        //given
        transactionTemplate.execute {
            storeRepository.deleteAllInBatch()
        }
        val accountancyResponse = InvoiceResponse(
            id = 1,
            orderId = 1,
            type = InvoiceTypeDTO.OUTCOME,
            total = BigDecimal("90.00"),
            status = InvoiceStatusDTO.PAID,
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

        // when
        val exception = assertThrows<IncorrectTypeOfInvoice> {
            storeService.receiveProduct(1)
        }

        // then
        assertThat(exception.message).contains("Invoice type must be income")
    }

    @Test
    fun `should not receive products if status not paid`() {

        //given
        transactionTemplate.execute {
            storeRepository.deleteAllInBatch()
        }
        val accountancyResponse = InvoiceResponse(
            id = 1,
            orderId = 1,
            type = InvoiceTypeDTO.INCOME,
            status = InvoiceStatusDTO.CREATED,
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

        // when
        val exception = assertThrows<IncorrectStatusOfInvoice> {
            storeService.receiveProduct(1)
        }

        // then
        assertThat(exception.message).contains("Invoice is not paid")
    }

    @Test
    fun `should return products to reduce if products available`() {

        // given
        val products = listOf(
            DeliverProductsQuantityRequest(
                id = 1,
                quantity = 2
            ),
            DeliverProductsQuantityRequest(
                id = 2,
                quantity = 3
            )
        )

        // and
        val productResponse = listOf(
            ProductDetailsResponse(
                id = 1,
                quantity = 10
            ),
            ProductDetailsResponse(
                id = 2,
                quantity = 15
            )
        )

        //and
        stubFor(
            get("/api/v1/products/details?ids=1&ids=2")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
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

        // when
        val availability = transactionTemplate.execute {
            storeService.checkAvailability(products)
        }

        assertThat(availability).hasSize(2)
    }

    @Test
    fun `should not return  products to reduce if products unavailable`() {

        // given
        val products = listOf(
            DeliverProductsQuantityRequest(
                id = 1,
                quantity = 2
            ),
            DeliverProductsQuantityRequest(
                id = 2,
                quantity = 3
            )
        )

        // and
        val productResponse = listOf(
            ProductDetailsResponse(
                id = 1,
                quantity = 0
            ),
            ProductDetailsResponse(
                id = 2,
                quantity = 0
            )
        )

        //and
        stubFor(
            get("/api/v1/products/details?ids=1&ids=2")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
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
        // when
        val exception = assertThrows<InsufficientAmountOfProductException> {
            storeService.checkAvailability(products)
        }

        assertThat(exception.message).contains("Insufficient amount of store with price item id =")
    }
}
