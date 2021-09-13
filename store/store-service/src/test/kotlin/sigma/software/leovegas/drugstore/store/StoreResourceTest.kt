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
import sigma.software.leovegas.drugstore.accountancy.api.ItemDTO
import sigma.software.leovegas.drugstore.infrastructure.extensions.get
import sigma.software.leovegas.drugstore.infrastructure.extensions.respTypeRef
import sigma.software.leovegas.drugstore.product.api.DeliverProductsQuantityRequest
import sigma.software.leovegas.drugstore.product.api.DeliverProductsResponse
import sigma.software.leovegas.drugstore.product.api.ProductDetailsResponse
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

        val orderNumber: Long = 1

        // and
        transactionTemplate.execute {
            storeRepository.save(
                TransferCertificate(
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
        assertThat(body[0].orderNumber).isEqualTo(1)
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
        assertThat(body[0].orderNumber).isEqualTo(1)
        assertThat(body[1].orderNumber).isEqualTo(2)
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

        val httpEntity = HttpEntity(1)
        // when
        val response = restTemplate.exchange(
            "$baseUrl/api/v1/store/receive",
            HttpMethod.PUT, httpEntity, respTypeRef<TransferCertificateResponse>()
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.ACCEPTED)

        // and
        val body = response.body.get("body")
        assertThat(body).isNotNull
        assertThat(body.orderNumber).isEqualTo(1)
        assertThat(body.status).isEqualTo(TransferStatusDTO.RECEIVED)
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
        val body = response.body.get("body")
        assertThat(body).isNotNull
        assertThat(body).isNotNull
        assertThat(body.orderNumber).isEqualTo(1)
        assertThat(body.status).isEqualTo(TransferStatusDTO.DELIVERED)
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
            WireMock.get("/api/v1/products/details?ids=${productResponse[0].productNumber}&ids=${productResponse[1].productNumber}")
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
        val orderNumber: Long = 1

        // when
        val response = restTemplate.exchange(
            "$baseUrl/api/v1/store/check-transfer/1",
            HttpMethod.GET, null, respTypeRef<Long>()
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        // and
        val body = response.body.get()
        assertThat(body).isEqualTo(orderNumber)
    }
}
