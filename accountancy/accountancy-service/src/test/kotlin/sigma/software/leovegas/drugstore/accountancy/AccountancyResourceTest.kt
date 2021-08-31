package sigma.software.leovegas.drugstore.accountancy

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.put
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
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
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpMethod.POST
import org.springframework.http.HttpMethod.PUT
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.accountancy.api.InvoiceRequest
import sigma.software.leovegas.drugstore.accountancy.api.InvoiceResponse
import sigma.software.leovegas.drugstore.accountancy.api.InvoiceStatusDTO
import sigma.software.leovegas.drugstore.accountancy.api.MarkupUpdateRequest
import sigma.software.leovegas.drugstore.accountancy.api.MarkupUpdateResponse
import sigma.software.leovegas.drugstore.accountancy.api.PriceItemRequest
import sigma.software.leovegas.drugstore.accountancy.api.PriceItemResponse
import sigma.software.leovegas.drugstore.accountancy.api.PurchasedCostsRequest
import sigma.software.leovegas.drugstore.accountancy.api.PurchasedCostsResponse
import sigma.software.leovegas.drugstore.accountancy.api.PurchasedItemDTO
import sigma.software.leovegas.drugstore.accountancy.client.AccountancyProperties
import sigma.software.leovegas.drugstore.extensions.respTypeRef
import sigma.software.leovegas.drugstore.order.api.OrderDetailsDTO
import sigma.software.leovegas.drugstore.order.api.OrderItemDetailsDTO
import sigma.software.leovegas.drugstore.order.api.OrderResponse
import sigma.software.leovegas.drugstore.order.api.OrderStatusDTO
import sigma.software.leovegas.drugstore.store.api.StoreResponse
import sigma.software.leovegas.drugstore.store.api.UpdateStoreRequest

@DisplayName("Accountancy Resource test")
@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureWireMock(port = 8082)
class AccountancyResourceTest @Autowired constructor(
    @LocalServerPort val port: Int,
    val restTemplate: TestRestTemplate,
    val transactionalTemplate: TransactionTemplate,
    val purchasedCostsRepository: PurchasedCostsRepository,
    val priceItemRepository: PriceItemRepository,
    val invoiceRepository: InvoiceRepository,
    val accountancyProperties: AccountancyProperties,
    val objectMapper: ObjectMapper
) {

    lateinit var baseUrl: String
    private val wireMockServerStoreClient = WireMockServer(wireMockConfig().port(8083))

    @BeforeEach
    fun setup() {
        baseUrl = "http://${accountancyProperties.host}:$port"
    }

    @Test
    fun `should create price item`() {

        // given
        val httpEntity = HttpEntity(
            PriceItemRequest(
                productId = 1L,
                price = BigDecimal.TEN,
            )
        )

        // when
        val response = restTemplate.exchange(
            "$baseUrl/api/v1/accountancy/price-item",
            POST,
            httpEntity,
            respTypeRef<PriceItemResponse>()
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)

        // and
        val body = response.body ?: fail("body may not be null")
        assertThat(body.id).isNotNull
        assertThat(body.price).isEqualTo(BigDecimal.TEN)
        assertThat(body.createdAt).isBeforeOrEqualTo(LocalDateTime.now())
    }

    @Test
    fun `should create invoice`() {

        // setup
        wireMockServerStoreClient.start()

        // and
        transactionalTemplate.execute {
            invoiceRepository.deleteAll()
        } ?: fail("result is expected")

        // and
        val httpEntity = HttpEntity(
            InvoiceRequest(
                orderId = 1L
            )
        )

        val orderDetails = OrderDetailsDTO(
            orderItemDetails = listOf(
                OrderItemDetailsDTO(
                    priceItemId = 1L,
                    name = "test1",
                    price = BigDecimal("20.00"),
                    quantity = 3,
                ),
                OrderItemDetailsDTO(
                    priceItemId = 2L,
                    name = "test2",
                    price = BigDecimal("10.00"),
                    quantity = 3,
                )
            ),
            total = BigDecimal("90").setScale(2)
        )

        stubFor(
            put("/api/v1/orders/change-status/1")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
                .withRequestBody(
                    EqualToPattern(
                        objectMapper
                            .writerWithDefaultPrettyPrinter()
                            .writeValueAsString(OrderStatusDTO.BOOKED)
                    )
                )
                .willReturn(
                    aResponse()
                        .withBody(
                            objectMapper
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(OrderResponse(orderStatus = OrderStatusDTO.BOOKED))
                        )
                        .withStatus(HttpStatus.OK.value())
                )
        )

        stubFor(
            get("/api/v1/orders/1/details")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(
                    aResponse()
                        .withBody(
                            objectMapper
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(orderDetails)
                        )
                        .withStatus(HttpStatus.OK.value())
                )
        )

        // and
        val storeResponse = listOf(
            StoreResponse(
                id = 1L,
                priceItemId = 1L,
                quantity = 2
            )
        )

        wireMockServerStoreClient.stubFor(
            put("/api/v1/store/reduce")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
                .withRequestBody(
                    EqualToPattern(
                        objectMapper
                            .writerWithDefaultPrettyPrinter()
                            .writeValueAsString(
                                listOf(
                                    UpdateStoreRequest(1L, 3),
                                    UpdateStoreRequest(2L, 3)
                                )
                            )
                    )
                )
                .willReturn(
                    aResponse()
                        .withBody(
                            objectMapper
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(storeResponse)
                        )
                        .withStatus(HttpStatus.OK.value())
                )
        )

        // when
        val response = restTemplate.exchange(
            "$baseUrl/api/v1/accountancy/invoice",
            POST,
            httpEntity,
            respTypeRef<InvoiceResponse>()
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)

        // and
        val body = response.body ?: fail("body may not be null")
        assertThat(body.id).isNotNull
        assertThat(body.total).isEqualTo(BigDecimal("90.00"))
        assertThat(body.createdAt).isBefore(LocalDateTime.now())

        // and
        wireMockServerStoreClient.stop()
    }

    @Test
    fun `should update price item`() {

        // given
        val priceItem = PriceItem(
            productId = 1L,
            price = BigDecimal.ONE,
            markup = BigDecimal.ZERO
        )

        val savedPriceItem = transactionalTemplate.execute {
            priceItemRepository.save(priceItem)
        } ?: fail("result is expected")

        val httpEntity = HttpEntity(
            PriceItemRequest(
                productId = 1L,
                price = BigDecimal.TEN,
            )
        )

        // when
        val response = restTemplate.exchange(
            "$baseUrl/api/v1/accountancy/price-item/${savedPriceItem.id}",
            PUT,
            httpEntity,
            respTypeRef<PriceItemResponse>()
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.ACCEPTED)

        // and
        val body = response.body ?: fail("body may not be null")
        assertThat(body.price).isEqualTo(httpEntity.body?.price)
        assertThat(body.createdAt).isBefore(body.updatedAt)
    }

    @Test
    fun `should get products price`() {

        // given
        transactionalTemplate.execute {
            priceItemRepository.deleteAll()
        } ?: fail("result is expected")

        // and
        val priceItem = PriceItem(
            productId = 1L,
            price = BigDecimal("10.00"),
            markup = BigDecimal.ZERO
        )

        // and
        val savedPriceItem = transactionalTemplate.execute {
            priceItemRepository.save(priceItem)
        } ?: fail("result is expected")

        // when
        val response = restTemplate.exchange(
            "$baseUrl/api/v1/accountancy/product-price",
            GET,
            null,
            respTypeRef<List<PriceItemResponse>>()
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        // and
        val body = response.body ?: fail("body may not be null")
        assertThat(body.size).isEqualTo(1)
        assertThat(body[0].price).isEqualTo(BigDecimal("10.00"))
    }

    @Test
    fun `should get products price by products ids`() {

        // given
        transactionalTemplate.execute {
            priceItemRepository.deleteAll()
        } ?: fail("result is expected")

        // and
        val savedPriceItem = transactionalTemplate.execute {
            priceItemRepository.saveAll(
                listOf(
                    PriceItem(
                        productId = 1L,
                        price = BigDecimal.ONE,
                        markup = BigDecimal.ZERO
                    ),
                    PriceItem(
                        productId = 2L,
                        price = BigDecimal.ONE,
                        markup = BigDecimal.ZERO
                    ),
                    PriceItem(
                        productId = 3L,
                        price = BigDecimal.ONE,
                        markup = BigDecimal.ZERO
                    )
                )
            )
        } ?: fail("result is expected")

        // when
        val response = restTemplate.exchange(
            "$baseUrl/api/v1/accountancy/price-by-product-ids?ids=1,2",
            GET,
            null,
            respTypeRef<List<PriceItemResponse>>()
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        // and
        val body = response.body ?: fail("body may not be null")
        assertThat(body.size).isEqualTo(2)
        assertThat(body[0].price).isEqualTo(BigDecimal("1.00"))
        assertThat(body[1].price).isEqualTo(BigDecimal("1.00"))
    }

    @Test
    fun `should get invoice by id`() {

        // given
        val savedInvoice = transactionalTemplate.execute {
            invoiceRepository.save(
                Invoice(
                    orderId = 1L,
                    total = BigDecimal("90.00")
                )
            )
        } ?: fail("result is expected")

        // when
        val response = restTemplate.exchange(
            "$baseUrl/api/v1/accountancy/invoice/${savedInvoice.id}",
            GET,
            null,
            respTypeRef<InvoiceResponse>()
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        // and
        val body = response.body ?: fail("body may not be null")
        assertThat(body.id).isNotNull
        assertThat(body.total).isEqualTo(BigDecimal("90.00"))
        assertThat(body.createdAt).isBefore(LocalDateTime.now())
    }

    @Test
    fun `should get invoice by order id`() {

        // given
        transactionalTemplate.execute {
            invoiceRepository.deleteAll()
        }

        // given
        val savedInvoice = transactionalTemplate.execute {
            invoiceRepository.save(
                Invoice(
                    orderId = 1,
                    total = BigDecimal("90.00")
                )
            )
        } ?: fail("result is expected")

        // when
        val response = restTemplate.exchange(
            "$baseUrl/api/v1/accountancy/invoice/order-id/1",
            GET,
            null,
            respTypeRef<InvoiceResponse>()
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        // and
        val body = response.body ?: fail("body may not be null")
        assertThat(body.id).isEqualTo(savedInvoice.id)
        assertThat(body.orderId).isEqualTo(savedInvoice.orderId)
        assertThat(body.total).isEqualTo(BigDecimal("90.00"))
    }

    @Test
    fun `should refund invoice`() {

        // given
        val savedInvoice = transactionalTemplate.execute {
            invoiceRepository.save(
                Invoice(
                    orderId = 1L,
                    total = BigDecimal("90.00"),
                    status = InvoiceStatus.PAID,
                    productItems = setOf(
                        ProductItem(
                            priceItemId = 1L,
                            name = "test",
                            price = BigDecimal("30"),
                            quantity = 3
                        )
                    )
                )
            )
        } ?: fail("result is expected")

        stubFor(
            put("/api/v1/orders/change-status/${savedInvoice.orderId}")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
                .withRequestBody(
                    EqualToPattern(
                        objectMapper
                            .writerWithDefaultPrettyPrinter()
                            .writeValueAsString(OrderStatusDTO.REFUND)
                    )
                )
                .willReturn(
                    aResponse()
                        .withBody(
                            objectMapper
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(OrderResponse(orderStatus = OrderStatusDTO.REFUND))
                        )
                        .withStatus(HttpStatus.OK.value())
                )
        )

        // when
        val response = restTemplate.exchange(
            "$baseUrl/api/v1/accountancy/invoice/refund/${savedInvoice.id}",
            PUT,
            null,
            respTypeRef<InvoiceResponse>()
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.ACCEPTED)

        // and
        val body = response.body ?: fail("body may not be null")
        assertThat(body.id).isEqualTo(savedInvoice.id ?: -1)
        assertThat(body.status).isEqualTo(InvoiceStatusDTO.REFUND)
    }

    @Test
    fun `should pay invoice`() {

        // given
        val savedInvoice = transactionalTemplate.execute {
            invoiceRepository.save(
                Invoice(
                    orderId = 1L,
                    total = BigDecimal("90.00"),
                    status = InvoiceStatus.CREATED,
                    productItems = setOf(
                        ProductItem(
                            priceItemId = 1L,
                            name = "test",
                            price = BigDecimal("30"),
                            quantity = 3
                        )
                    ),
                )
            )
        } ?: fail("result is expected")

        // and
        stubFor(
            put("/api/v1/orders/change-status/${savedInvoice.orderId}")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
                .withRequestBody(
                    EqualToPattern(
                        objectMapper
                            .writerWithDefaultPrettyPrinter()
                            .writeValueAsString(OrderStatusDTO.PAID)
                    )
                )
                .willReturn(
                    aResponse()
                        .withBody(
                            objectMapper
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(OrderResponse(orderStatus = OrderStatusDTO.PAID))
                        )
                        .withStatus(HttpStatus.OK.value())
                )
        )

        // when
        val response = restTemplate.exchange(
            "$baseUrl/api/v1/accountancy/invoice/pay/${savedInvoice.id}",
            PUT,
            null,
            respTypeRef<InvoiceResponse>()
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.ACCEPTED)

        // and
        val body = response.body ?: fail("body may not be null")
        assertThat(body.id).isNotNull
        assertThat(body.status).isEqualTo(InvoiceStatusDTO.PAID)
    }

    @Test
    fun `should cancel invoice by id`() {

        // setup
        wireMockServerStoreClient.start()

        // and
        transactionalTemplate.execute {
            invoiceRepository.deleteAll()
        } ?: fail("result is expected")

        // and
        val savedInvoice = transactionalTemplate.execute {
            invoiceRepository.save(
                Invoice(
                    orderId = 1L,
                    total = BigDecimal("90.00"),
                    productItems = setOf(
                        ProductItem(
                            priceItemId = 1L,
                            name = "test",
                            quantity = 2,
                            price = BigDecimal("10.00")
                        )
                    )
                )
            )
        } ?: fail("result is expected")

        stubFor(
            put("/api/v1/orders/change-status/${savedInvoice.orderId}")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
                .withRequestBody(
                    EqualToPattern(
                        objectMapper
                            .writerWithDefaultPrettyPrinter()
                            .writeValueAsString(OrderStatusDTO.CANCELLED)
                    )
                )
                .willReturn(
                    aResponse()
                        .withBody(
                            objectMapper
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(OrderResponse(orderStatus = OrderStatusDTO.CANCELLED))
                        )
                        .withStatus(HttpStatus.OK.value())
                )
        )

        // and
        val storeResponse = listOf(
            StoreResponse(
                id = 1L,
                priceItemId = 1L,
                quantity = 2
            )
        )

        wireMockServerStoreClient.stubFor(
            put("/api/v1/store/increase")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
                .withRequestBody(
                    EqualToPattern(
                        objectMapper
                            .writerWithDefaultPrettyPrinter()
                            .writeValueAsString(
                                listOf(
                                    UpdateStoreRequest(1L, 2)
                                )
                            )
                    )
                )
                .willReturn(
                    aResponse()
                        .withBody(
                            objectMapper
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(storeResponse)
                        )
                        .withStatus(HttpStatus.OK.value())
                )
        )

        // when
        val response = restTemplate.exchange(
            "$baseUrl/api/v1/accountancy/invoice/cancel/${savedInvoice.id}",
            PUT,
            null,
            respTypeRef<InvoiceResponse>()
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.ACCEPTED)

        // and
        val body = response.body ?: fail("body may not be null")
        assertThat(body.id).isNotNull
        assertThat(body.total).isEqualTo(BigDecimal("90.00"))
        assertThat(body.status).isEqualTo(InvoiceStatus.CANCELLED.toDTO())

        wireMockServerStoreClient.stop()
    }

    @Test
    fun `should get price items by ids`() {

        // given
        transactionalTemplate.execute {
            priceItemRepository.deleteAll()
        } ?: fail("result is expected")

        val ids = transactionalTemplate.execute {
            priceItemRepository.saveAll(
                listOf(
                    PriceItem(
                        productId = 1L,
                        price = BigDecimal.ONE,
                        markup = BigDecimal.ZERO
                    ),
                    PriceItem(
                        productId = 2L,
                        price = BigDecimal.TEN,
                        markup = BigDecimal.ZERO
                    )
                )
            )
        }?.map { it.id } ?: fail("result is expected")
        println(ids)
        // when
        val response = restTemplate.exchange(
            "$baseUrl/api/v1/accountancy/price-items-by-ids?ids=${ids[0]},${ids[1]}",
            GET,
            null,
            respTypeRef<List<PriceItemResponse>>()
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        // and
        val body = response.body ?: fail("body may not be null")
        assertThat(body.size).isEqualTo(2)
        assertThat(body[0].price).isEqualTo(BigDecimal("1.00"))
        assertThat(body[1].price).isEqualTo(BigDecimal("10.00"))
    }

    @Test
    fun `should get markups by price items ids`() {

        // given
        transactionalTemplate.execute {
            priceItemRepository.deleteAll()
        } ?: fail("result is expected")

        val saved = transactionalTemplate.execute {
            priceItemRepository.saveAll(
                listOf(
                    PriceItem(
                        productId = 1L,
                        price = BigDecimal.ONE,
                        markup = BigDecimal("0.10")
                    ),
                    PriceItem(
                        productId = 2L,
                        price = BigDecimal.TEN,
                        markup = BigDecimal("0.20")
                    )
                )
            )
        } ?: fail("result is expected")

        // when
        val response = restTemplate.exchange(
            "$baseUrl/api/v1/accountancy/price-item/markup?ids=${saved[0].id}",
            GET,
            null,
            respTypeRef<List<MarkupUpdateResponse>>()
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        // and
        val body = response.body ?: fail("body may not be null")
        assertThat(body.size).isEqualTo(1)
        assertThat(body[0].markup).isEqualTo(BigDecimal("0.10"))
    }

    @Test
    fun `should update markups`() {

        // given
        transactionalTemplate.execute {
            priceItemRepository.deleteAll()
        } ?: fail("result is expected")

        val saved = transactionalTemplate.execute {
            priceItemRepository.saveAll(
                listOf(
                    PriceItem(
                        productId = 1L,
                        price = BigDecimal.ONE,
                        markup = BigDecimal("0.10")
                    ),
                    PriceItem(
                        productId = 2L,
                        price = BigDecimal.TEN,
                        markup = BigDecimal("0.20")
                    )
                )
            )
        } ?: fail("result is expected")

        // and
        val httpEntity = HttpEntity(
            listOf(
                MarkupUpdateRequest(
                    priceItemId = saved[0].id ?: -1,
                    markup = BigDecimal("0.30")
                ),
                MarkupUpdateRequest(
                    priceItemId = saved[1].id ?: -1,
                    markup = BigDecimal("0.30")
                )
            )
        )

        // when
        val response = restTemplate.exchange(
            "$baseUrl/api/v1/accountancy/price-item/markup",
            PUT,
            httpEntity,
            respTypeRef<List<MarkupUpdateResponse>>()
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.ACCEPTED)

        // and
        val body = response.body ?: fail("body may not be null")
        assertThat(body.size).isEqualTo(2)
        assertThat(body[0].markup).isEqualTo(BigDecimal("0.30"))
        assertThat(body[1].markup).isEqualTo(BigDecimal("0.30"))
    }

    @Test
    fun `should get markups`() {

        // given
        transactionalTemplate.execute {
            priceItemRepository.deleteAll()
        } ?: fail("result is expected")

        transactionalTemplate.execute {
            priceItemRepository.saveAll(
                listOf(
                    PriceItem(
                        productId = 1L,
                        price = BigDecimal.ONE,
                        markup = BigDecimal("0.20")
                    ),
                    PriceItem(
                        productId = 2L,
                        price = BigDecimal.TEN,
                        markup = BigDecimal("0.20")
                    )
                )
            )
        } ?: fail("result is expected")

        // when
        val response = restTemplate.exchange(
            "$baseUrl/api/v1/accountancy/price-item/markup",
            GET,
            null,
            respTypeRef<List<MarkupUpdateResponse>>()
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        // and
        val body = response.body ?: fail("body may not be null")
        assertThat(body.size).isEqualTo(2)
        assertThat(body[0].markup).isEqualTo(BigDecimal("0.20"))
        assertThat(body[1].markup).isEqualTo(BigDecimal("0.20"))
    }

    @Test
    fun `should create purchased costs`() {

        // setup
        wireMockServerStoreClient.start()

        // given
        transactionalTemplate.execute {
            priceItemRepository.deleteAll()
        }

        // and
        transactionalTemplate.execute {
            purchasedCostsRepository.deleteAll()
        }

        // and
        val priceItem = transactionalTemplate.execute {
            priceItemRepository.save(
                PriceItem(
                    productId = 1L,
                    price = BigDecimal("25.50"),
                    markup = BigDecimal.ZERO
                )
            )
        } ?: fail("result is expected")

        // and
        val storeIncreaseResponse = listOf(
            StoreResponse(
                id = 1,
                priceItemId = priceItem.id ?: -1,
                quantity = 12
            )
        )

        // and
        val storeGetResponse = listOf(
            StoreResponse(
                id = 1,
                priceItemId = priceItem.id ?: -1,
                quantity = 2
            )
        )

        // and
        wireMockServerStoreClient.stubFor(
            get("/api/v1/store/price-ids/?ids=${priceItem.id}")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(
                    aResponse()
                        .withBody(
                            objectMapper
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(storeGetResponse)
                        )
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                )
        )

        // and
        wireMockServerStoreClient.stubFor(
            put("/api/v1/store/increase")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
                .withRequestBody(
                    EqualToPattern(
                        objectMapper
                            .writerWithDefaultPrettyPrinter()
                            .writeValueAsString(
                                listOf(
                                    UpdateStoreRequest(priceItem.id ?: -1, 10)
                                )
                            )
                    )
                )
                .willReturn(
                    aResponse()
                        .withBody(
                            objectMapper
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(storeIncreaseResponse)
                        )
                        .withStatus(HttpStatus.OK.value())
                )
        )

        // and
        val httpEntity = HttpEntity(
            PurchasedCostsRequest(
                priceItemId = priceItem.id ?: -1,
                quantity = 10,
            )
        )

        // when
        val response = restTemplate.exchange(
            "$baseUrl/api/v1/accountancy/purchased-costs",
            POST,
            httpEntity,
            respTypeRef<PurchasedCostsResponse>()
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)

        // and
        val body = response.body ?: fail("body may not be null")
        assertThat(body.id).isNotNull
        assertThat(body.quantity).isEqualTo(10)
        assertThat(body.priceItemId).isEqualTo(priceItem.id)
        assertThat(body.dateOfPurchase).isBeforeOrEqualTo(LocalDateTime.now())

        wireMockServerStoreClient.stop()
    }

    @Test
    fun `should get purchased items`() {

        // given
        transactionalTemplate.execute {
            invoiceRepository.deleteAll()
        }

        // and
        val created = transactionalTemplate.execute {
            invoiceRepository.saveAll(
                listOf(
                    Invoice(
                        orderId = 1,
                        total = BigDecimal("50.00"),
                        status = InvoiceStatus.PAID,
                        productItems = setOf(
                            ProductItem(
                                priceItemId = 1,
                                name = "test1",
                                price = BigDecimal.TEN,
                                quantity = 5
                            )
                        )
                    ),
                    Invoice(
                        orderId = 2,
                        total = BigDecimal("50.00"),
                        status = InvoiceStatus.PAID,
                        productItems = setOf(
                            ProductItem(
                                priceItemId = 1,
                                name = "test1",
                                price = BigDecimal.TEN,
                                quantity = 5
                            )
                        )
                    ),
                    Invoice(
                        orderId = 3,
                        total = BigDecimal("20.00"),
                        status = InvoiceStatus.PAID,
                        productItems = setOf(
                            ProductItem(
                                priceItemId = 2,
                                name = "test1",
                                price = BigDecimal.ONE,
                                quantity = 2
                            )
                        )
                    ),
                    Invoice(
                        orderId = 4,
                        total = BigDecimal("20.00"),
                        status = InvoiceStatus.CREATED,
                        productItems = setOf(
                            ProductItem(
                                priceItemId = 2,
                                name = "test1",
                                price = BigDecimal.ONE,
                                quantity = 2
                            )
                        )
                    )
                )
            )
        } ?: fail("result is expected")

        // when
        val response = restTemplate.exchange(
            "$baseUrl/api/v1/accountancy/past-purchased-items",
            GET,
            null,
            respTypeRef<List<PurchasedItemDTO>>()
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        // and
        val body = response.body ?: fail("body may not be null")
        assertThat(body).hasSize(2)
        assertThat(body[0].quantity).isEqualTo(10)
        assertThat(body[1].quantity).isEqualTo(2)
    }
}
