package sigma.software.leovegas.drugstore.store

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.matching.ContainsPattern
import com.github.tomakehurst.wiremock.matching.EqualToPattern
import java.math.BigDecimal
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.accountancy.api.InvoiceResponse
import sigma.software.leovegas.drugstore.accountancy.api.InvoiceStatusDTO
import sigma.software.leovegas.drugstore.infrastructure.extensions.respTypeRef
import sigma.software.leovegas.drugstore.order.api.OrderResponse
import sigma.software.leovegas.drugstore.order.api.OrderStatusDTO
import sigma.software.leovegas.drugstore.store.api.CreateStoreRequest
import sigma.software.leovegas.drugstore.store.api.StoreResponse
import sigma.software.leovegas.drugstore.store.api.UpdateStoreRequest

@DisplayName("StoreResource test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class StoreResourceTest @Autowired constructor(
    @LocalServerPort val port: Int,
    val restTemplate: TestRestTemplate,
    val transactionTemplate: TransactionTemplate,
    val storeRepository: StoreRepository,
    val objectMapper: ObjectMapper,
    val storeProperties: StoreProperties
) {
    lateinit var baseUrl: String

    @BeforeEach
    fun setup() {
        baseUrl = "http://${storeProperties.host}:$port"
    }

    @Test
    fun `should create store item`() {

        //given
        transactionTemplate.execute {
            storeRepository.deleteAllInBatch()
        }

        // and
        val httpEntity = HttpEntity(
            CreateStoreRequest(
                priceItemId = 1,
                quantity = 10
            )
        )

        // when
        val response = restTemplate.exchange(
            "$baseUrl/api/v1/store",
            HttpMethod.POST, httpEntity, respTypeRef<StoreResponse>()
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)

        // and
        val body = response.body ?: fail("body may not be null")
        assertThat(body.id).isNotNull
        assertThat(body.priceItemId).isEqualTo(1)
        assertThat(body.quantity).isEqualTo(10)
    }

    @Test
    fun `should get store items`() {

        //given
        transactionTemplate.execute {
            storeRepository.deleteAllInBatch()
        }

        // and
        val created = transactionTemplate.execute {
            storeRepository.saveAll(
                listOf(
                    Store(
                        priceItemId = 1,
                        quantity = 10
                    ), Store(
                        priceItemId = 2,
                        quantity = 5
                    )
                )
            )
        }

        // when
        val response = restTemplate.exchange(
            "$baseUrl/api/v1/store",
            HttpMethod.GET, null, respTypeRef<List<StoreResponse>>()
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        // and
        val body = response.body ?: fail("body may not be null")
        assertThat(body).isNotNull
        assertThat(body).hasSize(2)
    }

    @Test
    fun `should get store items by price items id`() {

        //given
        transactionTemplate.execute {
            storeRepository.deleteAllInBatch()
        }

        // and
        val ids = transactionTemplate.execute {
            storeRepository.saveAll(
                listOf(
                    Store(
                        priceItemId = 1,
                        quantity = 10
                    ), Store(
                        priceItemId = 2,
                        quantity = 5
                    )
                )
            )
        }?.map { it.priceItemId }

        // when
        val response = restTemplate.exchange(
            "$baseUrl/api/v1/store/price-ids/?ids=${ids?.get(0)}&ids=${ids?.get(1)}",
            HttpMethod.GET, null, respTypeRef<List<StoreResponse>>()
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        // and
        val body = response.body ?: fail("body may not be null")
        assertThat(body).isNotNull
        assertThat(body).hasSize(2)
        assertThat(body[0].priceItemId).isEqualTo(ids?.get(0) ?: -1)
        assertThat(body[1].priceItemId).isEqualTo(ids?.get(1) ?: -1)
    }

    @Test
    fun `should increase store items quantity`() {

        //given
        transactionTemplate.execute {
            storeRepository.deleteAllInBatch()
        }

        // and
        val httpEntity = HttpEntity(
            listOf(
                UpdateStoreRequest(
                    priceItemId = 1,
                    quantity = 5
                )
            )
        )

        // and
        val created = transactionTemplate.execute {
            storeRepository.save(
                Store(
                    priceItemId = 1,
                    quantity = 10
                )
            )
        }

        // when
        val response = restTemplate.exchange(
            "$baseUrl/api/v1/store/increase",
            HttpMethod.PUT, httpEntity, respTypeRef<List<StoreResponse>>()
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.ACCEPTED)

        // and
        val body = response.body ?: fail("body may not be null")
        assertThat(body).isNotNull
        assertThat(body).hasSize(1)
        assertThat(body[0].id).isEqualTo(created?.id ?: -1)
        assertThat(body[0].priceItemId).isEqualTo(created?.priceItemId ?: -1)
        assertThat(body[0].quantity).isEqualTo(15) // 10+5=15
    }

    @Test
    fun `should reduce store items quantity`() {

        //given
        transactionTemplate.execute {
            storeRepository.deleteAllInBatch()
        }

        // and
        val httpEntity = HttpEntity(
            listOf(
                UpdateStoreRequest(
                    priceItemId = 1,
                    quantity = 5
                )
            )
        )

        // and
        val created = transactionTemplate.execute {
            storeRepository.save(
                Store(
                    priceItemId = 1,
                    quantity = 10
                )
            )
        }

        // when
        val response = restTemplate.exchange(
            "$baseUrl/api/v1/store/reduce",
            HttpMethod.PUT, httpEntity, respTypeRef<List<StoreResponse>>()
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.ACCEPTED)

        // and
        val body = response.body ?: fail("body may not be null")
        assertThat(body).isNotNull
        assertThat(body).hasSize(1)
        assertThat(body[0].id).isEqualTo(created?.id ?: -1)
        assertThat(body[0].priceItemId).isEqualTo(created?.priceItemId ?: -1)
        assertThat(body[0].quantity).isEqualTo(5) // 10-5=5
    }

    @Test
    fun `should check store items quantity`() {

        //given
        transactionTemplate.execute {
            storeRepository.deleteAllInBatch()
        }

        // and
        val httpEntity = HttpEntity(
            listOf(
                UpdateStoreRequest(
                    priceItemId = 1,
                    quantity = 5
                )
            )
        )

        // and
        val created = transactionTemplate.execute {
            storeRepository.save(
                Store(
                    priceItemId = 1,
                    quantity = 10
                )
            )
        }

        // when
        val response = restTemplate.exchange(
            "$baseUrl/api/v1/store/check",
            HttpMethod.PUT, httpEntity, respTypeRef<List<StoreResponse>>()
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.ACCEPTED)

        // and
        val body = response.body ?: fail("body may not be null")
        assertThat(body).isNotNull
        assertThat(body).hasSize(1)
        assertThat(body[0].id).isEqualTo(created?.id ?: -1)
        assertThat(body[0].priceItemId).isEqualTo(created?.priceItemId ?: -1)
        assertThat(body[0].quantity).isEqualTo(10)
    }

    @Test
    fun `should deliver goods`() {

        // setup
        val wireMockServer8084 = WireMockServer(8084)
        val wireMockServer8082 = WireMockServer(8082)
        wireMockServer8084.start()
        wireMockServer8082.start()

        // given
        wireMockServer8084.stubFor(
            WireMock.get("/api/v1/accountancy/invoice/order-id/1")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(
                    WireMock.aResponse()
                        .withBody(
                            objectMapper
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(
                                    InvoiceResponse(
                                        id = 1,
                                        orderId = 1,
                                        total = BigDecimal("90.00"),
                                        status = InvoiceStatusDTO.PAID
                                    )
                                )
                        )
                )
        )

        // and
        wireMockServer8082.stubFor(
            WireMock.put("/api/v1/orders/change-status/1")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
                .withRequestBody(
                    EqualToPattern(
                        objectMapper
                            .writerWithDefaultPrettyPrinter()
                            .writeValueAsString(OrderStatusDTO.DELIVERED)
                    )
                )
                .willReturn(
                    WireMock.aResponse()
                        .withBody(
                            objectMapper
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(OrderResponse(orderStatus = OrderStatusDTO.DELIVERED))
                        )
                        .withStatus(HttpStatus.OK.value())
                )
        )


        // when
        val response = restTemplate.exchange(
            "$baseUrl/api/v1/store/delivery/1",
            HttpMethod.PUT, null, respTypeRef<String>()
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.ACCEPTED)

        // and
        val body = response.body ?: fail("body may not be null")
        assertThat(body).isEqualTo("DELIVERED")

        wireMockServer8082.stop()
        wireMockServer8084.stop()

    }
}
