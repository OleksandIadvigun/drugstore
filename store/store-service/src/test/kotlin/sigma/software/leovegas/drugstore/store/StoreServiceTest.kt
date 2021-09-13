package sigma.software.leovegas.drugstore.store

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.put
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.matching.ContainsPattern
import com.github.tomakehurst.wiremock.matching.EqualToPattern
import java.time.LocalDateTime
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.accountancy.api.ItemDTO
import sigma.software.leovegas.drugstore.infrastructure.extensions.get
import sigma.software.leovegas.drugstore.product.api.DeliverProductsQuantityRequest
import sigma.software.leovegas.drugstore.product.api.DeliverProductsResponse
import sigma.software.leovegas.drugstore.product.api.ProductDetailsResponse
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
            orderNumber = 1,
            status = TransferStatusDTO.RECEIVED,
            comment = "RECEIVED"
        )

        // when
        val created = transactionTemplate.execute {
            storeService.createTransferCertificate(transferCertificateRequest)
        }.get()

        // then
        assertThat(created.certificateNumber).isNotNull
        assertThat(created.orderNumber).isEqualTo(1)
        assertThat(created.status).isEqualTo(TransferStatusDTO.RECEIVED)
        assertThat(created.comment).isEqualTo("RECEIVED")

    }

    @Test
    fun `should get store transfer certificates by order id`() {

        // given
        transactionTemplate.execute {
            storeRepository.deleteAllInBatch()
        }

        // and
        val created = transactionTemplate.execute {
            storeRepository.save(
                TransferCertificate(
                    orderNumber = 1,
                    status = TransferStatus.RECEIVED,
                    comment = "RECEIVED"
                )
            )
        }.get()

        // when
        val actual = transactionTemplate.execute {
            storeService.getTransferCertificatesByOrderId(created.orderNumber)
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
                        orderNumber = 1,
                        status = TransferStatus.RECEIVED,
                        comment = "RECEIVED"
                    ),
                    TransferCertificate(
                        orderNumber = 2,
                        status = TransferStatus.DELIVERED,
                        comment = "DELIVERED"
                    )
                )
            )
        }

        // when
        val actual = transactionTemplate.execute {
            storeService.getTransferCertificates()
        }.get()

        // then
        assertThat(actual).hasSize(2)
        assertThat(actual[0].status).isEqualTo(TransferStatusDTO.RECEIVED)
        assertThat(actual[0].orderNumber).isEqualTo(1)
        assertThat(actual[1].status).isEqualTo(TransferStatusDTO.DELIVERED)
        assertThat(actual[1].orderNumber).isEqualTo(2)
    }

    @Test
    fun `should deliver products`() {

        // given
        transactionTemplate.execute {
            storeRepository.deleteAllInBatch()
        }

        // and
        val accountancyResponse = listOf(
            ItemDTO(productId = 1, quantity = 2),
            ItemDTO(productId = 2, quantity = 3),
        )

        // and
        val orderId: Long = 1

        // and
        stubFor(
            WireMock.get("/api/v1/accountancy/invoice/details/order-id/$orderId")
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
            ),
            DeliverProductsQuantityRequest(
                id = 2,
                quantity = 3
            )
        )

        //and
        val productResponse = listOf(
            DeliverProductsResponse(
                id = 1L,
                quantity = 5,
                updatedAt = LocalDateTime.now()
            ),
            DeliverProductsResponse(
                id = 2,
                quantity = 10,
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
                productNumber = 1,
                quantity = 10
            ),
            ProductDetailsResponse(
                productNumber = 2,
                quantity = 20
            ),
        )

        // and
        stubFor(
            WireMock.get("/api/v1/products/details?ids=${productDetailsResponse[0].productNumber}&ids=${productDetailsResponse[1].productNumber}")
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
            storeService.deliverProducts(orderId)
        }.get()

        // then
        assertThat(transferCertificate.certificateNumber).isNotNull
        assertThat(transferCertificate.orderNumber).isEqualTo(orderId)
        assertThat(transferCertificate.status).isEqualTo(TransferStatusDTO.DELIVERED)
    }

    @Test
    fun `should not deliver product if accountancy server not available`() {

        // setup
        transactionTemplate.execute {
            storeRepository.deleteAll()
        }

        // given
        val orderId: Long = 1

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
        val accountancyResponse = listOf(
            ItemDTO(productId = 3, quantity = 2),
            ItemDTO(productId = 4, quantity = 3),
        )

        // and
        val orderId: Long = 1

        // and
        stubFor(
            WireMock.get("/api/v1/accountancy/invoice/details/order-id/$orderId")
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
        val productDetailsResponse = listOf(
            ProductDetailsResponse(
                productNumber = 3,
                quantity = 10
            ),
            ProductDetailsResponse(
                productNumber = 4,
                quantity = 20
            ),
        )

        // and
        stubFor(
            WireMock.get("/api/v1/products/details?ids=${productDetailsResponse[0].productNumber}&ids=${productDetailsResponse[1].productNumber}")
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
        val exception = assertThrows<ProductServerResponseException> {
            storeService.deliverProducts(orderId)
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
        val accountancyResponse = listOf(
            ItemDTO(productId = 1, quantity = 2),
            ItemDTO(productId = 2, quantity = 3),
        )

        // and
        val orderId: Long = 1

        // and
        stubFor(
            WireMock.get("/api/v1/accountancy/invoice/details/order-id/${orderId}")
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
        val productResponse = listOf(
            DeliverProductsResponse(
                id = 1L,
                quantity = 5,
                updatedAt = LocalDateTime.now()
            ),
            DeliverProductsResponse(
                id = 2,
                quantity = 10,
                updatedAt = LocalDateTime.now()
            )

        )

        //and
        stubFor(
            put("/api/v1/products/receive")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
                .withRequestBody(
                    EqualToPattern(
                        objectMapper
                            .writerWithDefaultPrettyPrinter()
                            .writeValueAsString(listOf(1, 2))
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
            storeService.receiveProduct(orderId)
        }.get()

        // then
        assertThat(transferCertificate.certificateNumber).isNotNull
        assertThat(transferCertificate.orderNumber).isEqualTo(orderId)
        assertThat(transferCertificate.status).isEqualTo(TransferStatusDTO.RECEIVED)
    }

    @Test
    fun `should not receive product if accountancy server not available`() {

        // given
        transactionTemplate.execute {
            storeRepository.deleteAll()
        }

        // and
        val orderId: Long = 3

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
        val accountancyResponse = listOf(
            ItemDTO(productId = 5, quantity = 2),
            ItemDTO(productId = 6, quantity = 3),
        )

        // and
        val orderId: Long = 3

        // and
        stubFor(
            WireMock.get("/api/v1/accountancy/invoice/details/order-id/${orderId}")
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
        val exception = assertThrows<ProductServerResponseException> {
            storeService.receiveProduct(orderId)
        }

        // then
        assertThat(exception.message).startsWith("Ups... some problems in product service.")

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
                productNumber = 1,
                quantity = 10
            ),
            ProductDetailsResponse(
                productNumber = 2,
                quantity = 15
            )
        )

        //and
        stubFor(
            WireMock.get("/api/v1/products/details?ids=1&ids=2")
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
    fun `should not return products to reduce if products unavailable`() {

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
                productNumber = 1,
                quantity = 0
            ),
            ProductDetailsResponse(
                productNumber = 2,
                quantity = 0
            )
        )

        // and
        stubFor(
            WireMock.get("/api/v1/products/details?ids=1&ids=2")
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
        assertThat(exception.message).contains("Insufficient amount product with id =")
    }

    @Test
    fun `should return orderNumber if no transfer certificate found`() {

        // setup
        transactionTemplate.execute {
            storeRepository.deleteAll()
        }

        // given
        val orderNumber: Long = 1

        // when
        val actual = storeService.checkTransfer(orderNumber)

        // then
        assertThat(orderNumber).isEqualTo(actual)
    }

    @Test
    fun `should not return orderNumber if  transfer certificate was found`() {

        // setup
        transactionTemplate.execute {
            storeRepository.deleteAll()
        }

        // given
        val orderNumber: Long = 1

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
