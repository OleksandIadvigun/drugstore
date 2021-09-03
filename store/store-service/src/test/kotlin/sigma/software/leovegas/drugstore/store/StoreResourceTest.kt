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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.accountancy.api.InvoiceResponse
import sigma.software.leovegas.drugstore.accountancy.api.InvoiceStatusDTO
import sigma.software.leovegas.drugstore.accountancy.api.InvoiceTypeDTO
import sigma.software.leovegas.drugstore.accountancy.api.ProductItemDTO
import sigma.software.leovegas.drugstore.infrastructure.extensions.respTypeRef
import sigma.software.leovegas.drugstore.product.api.DeliverProductsQuantityRequest
import sigma.software.leovegas.drugstore.product.api.DeliverProductsResponse
import sigma.software.leovegas.drugstore.product.api.ProductDetailsResponse
import sigma.software.leovegas.drugstore.product.api.ProductStatusDTO
import sigma.software.leovegas.drugstore.product.api.ReceiveProductResponse
import sigma.software.leovegas.drugstore.store.api.TransferCertificateResponse
import sigma.software.leovegas.drugstore.store.api.TransferStatusDTO

@DisplayName("StoreResource test")
class StoreResourceTest @Autowired constructor(
    @LocalServerPort val port: Int,
    val restTemplate: TestRestTemplate,
    val transactionTemplate: TransactionTemplate,
    val storeRepository: StoreRepository,
    val objectMapper: ObjectMapper,
    val storeProperties: StoreProperties
) : WireMockTest() {

    lateinit var baseUrl: String

    @BeforeEach
    fun setup() {
        baseUrl = "http://${storeProperties.host}:$port"
    }

    @Test
    fun `should get transfer certificates by invoice id`() {

        // given
        transactionTemplate.execute {
            storeRepository.deleteAllInBatch()
        }

        // and
        transactionTemplate.execute {
            storeRepository.save(
                TransferCertificate(
                    invoiceId = 1,
                    status = TransferStatus.RECEIVED,
                    comment = "RECEIVED"
                )
            )
        } ?: fail("result is expected")

        // when
        val response = restTemplate.exchange(
            "$baseUrl/api/v1/store/transfer-certificate/invoice/1",
            HttpMethod.GET, null, respTypeRef<List<TransferCertificateResponse>>()
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        // and
        val body = response.body ?: fail("body may not be null")
        assertThat(body).isNotNull
        assertThat(body).hasSize(1)
        assertThat(body[0].invoiceId).isEqualTo(1)
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
        val response = restTemplate.exchange(
            "$baseUrl/api/v1/store/transfer-certificate",
            HttpMethod.GET, null, respTypeRef<List<TransferCertificateResponse>>()
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        // and
        val body = response.body ?: fail("body may not be null")
        assertThat(body).isNotNull
        assertThat(body).hasSize(2)
        assertThat(body[0].invoiceId).isEqualTo(1)
        assertThat(body[1].invoiceId).isEqualTo(2)
    }

    @Test
    fun `should receive products`() {

        // given
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
        val httpEntity = HttpEntity(1)

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

        val invoiceReceived = InvoiceResponse(
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
            put("/api/v1/accountancy/invoice/receive/1")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(
                    aResponse()
                        .withBody(
                            objectMapper
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(invoiceReceived)
                        )
                )
        )

        // when
        val response = restTemplate.exchange(
            "$baseUrl/api/v1/store/receive",
            HttpMethod.PUT, httpEntity, respTypeRef<TransferCertificateResponse>()
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.ACCEPTED)

        // and
        val body = response.body ?: fail("body may not be null")
        assertThat(body).isNotNull
        assertThat(body.invoiceId).isEqualTo(1)
        assertThat(body.status).isEqualTo(TransferStatusDTO.RECEIVED)
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

        // and
        val invoiceDelivered = InvoiceResponse(
            id = 1,
            orderId = 1,
            type = InvoiceTypeDTO.INCOME,
            status = InvoiceStatusDTO.DELIVERED,
            productItems = setOf(
                ProductItemDTO(productId = 1, quantity = 2)
            )
        )

        // and
        stubFor(
            put("/api/v1/accountancy/invoice/deliver/1")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(
                    aResponse()
                        .withBody(
                            objectMapper
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(invoiceDelivered)
                        )
                )
        )

        // and
        val httpEntity = HttpEntity(
            1
        )

        // when
        val response = restTemplate.exchange(
            "$baseUrl/api/v1/store/deliver",
            HttpMethod.PUT, httpEntity, respTypeRef<TransferCertificateResponse>()
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.ACCEPTED)

        // and
        val body = response.body ?: fail("body may not be null")
        assertThat(body).isNotNull
        assertThat(body).isNotNull
        assertThat(body.invoiceId).isEqualTo(1)
        assertThat(body.status).isEqualTo(TransferStatusDTO.DELIVERED)
    }

    @Test
    fun `should return received products`() {

        //given
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

        // and
        val httpEntity = HttpEntity(1)

        // when
        val response = restTemplate.exchange(
            "$baseUrl/api/v1/store/return",
            HttpMethod.PUT, httpEntity, respTypeRef<TransferCertificateResponse>()
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.ACCEPTED)

        // and
        val body = response.body ?: fail("body may not be null")
        assertThat(body).isNotNull
        assertThat(body).isNotNull
        assertThat(body.invoiceId).isEqualTo(1)
        assertThat(body.status).isEqualTo(TransferStatusDTO.RETURN)
    }

    @Test
    fun `should return delivered products`() {

        //given
        transactionTemplate.execute {
            storeRepository.deleteAllInBatch()
        }

        val accountancyResponse = InvoiceResponse(
            id = 1,
            orderId = 1,
            type = InvoiceTypeDTO.OUTCOME,
            status = InvoiceStatusDTO.DELIVERED,
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
                quantity = 3,
                updatedAt = LocalDateTime.now()
            )
        )

        //and
        stubFor(
            put("/api/v1/products/return")
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
        val httpEntity = HttpEntity(1)

        // when
        val response = restTemplate.exchange(
            "$baseUrl/api/v1/store/return",
            HttpMethod.PUT, httpEntity, respTypeRef<TransferCertificateResponse>()
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.ACCEPTED)

        // and
        val body = response.body ?: fail("body may not be null")
        assertThat(body).isNotNull
        assertThat(body).isNotNull
        assertThat(body.invoiceId).isEqualTo(1)
        assertThat(body.status).isEqualTo(TransferStatusDTO.RETURN)
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
        val httpEntity = HttpEntity(products)

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
        val response = restTemplate.exchange(
            "$baseUrl/api/v1/store/availability",
            HttpMethod.PUT, httpEntity, respTypeRef<List<DeliverProductsQuantityRequest>>()
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.ACCEPTED)

        // and
        val body = response.body ?: fail("body may not be null")
        assertThat(body).hasSize(2)
    }
}
