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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.accountancy.api.InvoiceRequest
import sigma.software.leovegas.drugstore.accountancy.api.InvoiceStatusDTO
import sigma.software.leovegas.drugstore.accountancy.api.MarkupUpdateRequest
import sigma.software.leovegas.drugstore.accountancy.api.PriceItemRequest
import sigma.software.leovegas.drugstore.accountancy.api.PriceItemResponse
import sigma.software.leovegas.drugstore.accountancy.api.PurchasedCostsRequest
import sigma.software.leovegas.drugstore.order.api.OrderDetailsDTO
import sigma.software.leovegas.drugstore.order.api.OrderItemDetailsDTO
import sigma.software.leovegas.drugstore.order.api.OrderResponse
import sigma.software.leovegas.drugstore.order.api.OrderStatusDTO
import sigma.software.leovegas.drugstore.store.api.StoreResponse
import sigma.software.leovegas.drugstore.store.api.UpdateStoreRequest

@AutoConfigureTestDatabase
@AutoConfigureWireMock(port = 8082)
@DisplayName("Accountancy Service test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AccountancyServiceTest @Autowired constructor(
    val service: AccountancyService,
    val transactionTemplate: TransactionTemplate,
    val invoiceRepository: InvoiceRepository,
    val objectMapper: ObjectMapper,
    val priceItemRepository: PriceItemRepository,
    val purchasedCostsRepository: PurchasedCostsRepository
) {

    private val wireMockServerStoreClient = WireMockServer(wireMockConfig().port(8083))

    @Test
    fun `should create price item`() {

        // given
        val priceItem = PriceItemRequest(
            productId = 1L,
            price = BigDecimal("25.50"),
        )

        //and
        val priceItemResponse = PriceItemResponse(
            productId = 1L,
            price = BigDecimal("25.50"),
        )

        // when
        val actual = service.createPriceItem(priceItem)

        // then
        assertThat(actual).isNotNull
        assertThat(actual.id).isNotNull
        assertEquals(priceItemResponse.productId, actual.productId)
        assertEquals(priceItemResponse.price, actual.price)
        assertThat(actual.createdAt).isBefore(LocalDateTime.now())
    }

    @Test
    fun `should create invoice`() {

        // given
        transactionTemplate.execute {
            invoiceRepository.deleteAll()
        }

        // and
        wireMockServerStoreClient.start()

        // and
        val invoiceRequest = InvoiceRequest(
            orderId = 1L
        )

        val orderDetail = OrderDetailsDTO(
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
            put("/api/v1/orders/change-status/${invoiceRequest.orderId}")
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
                                .writeValueAsString(orderDetail)
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
        val actual = service.createInvoice(invoiceRequest)

        // then
        assertThat(actual).isNotNull
        assertThat(actual.id).isNotNull
        assertThat(actual.total).isEqualTo(orderDetail.total)
        assertThat(actual.createdAt).isBeforeOrEqualTo(LocalDateTime.now())

        // and
        wireMockServerStoreClient.stop()
    }

    @Test
    fun `should not create invoice with order id already in the another invoice`() {

        // given
        transactionTemplate.execute {
            invoiceRepository.deleteAll()
        }

        // given
        val savedInvoice = transactionTemplate.execute {
            invoiceRepository.save(
                Invoice(
                    orderId = 1,
                    total = BigDecimal("90.00"),
                    productItems = setOf(
                        ProductItem(
                            name = "test",
                            price = BigDecimal("30"),
                            quantity = 3
                        )
                    )
                )
            )
        } ?: fail("result is expected")

        // and
        val invoiceRequest = InvoiceRequest(
            orderId = 1
        )

        // when
        val exception = assertThrows<OrderAlreadyHaveInvoice> {
            service.createInvoice(invoiceRequest)
        }

        // then
        assertThat(exception.message).contains("This order already have some invoice")
    }

    @Test
    fun `should get invoice by id`() {

        // given
        val savedInvoice = transactionTemplate.execute {
            invoiceRepository.save(
                Invoice(
                    orderId = 1L,
                    total = BigDecimal("90.00"),
                    productItems = setOf(
                        ProductItem(
                            name = "test",
                            price = BigDecimal("30"),
                            quantity = 3
                        )
                    )
                )
            )
        } ?: fail("result is expected")

        // when
        val actual = service.getInvoiceById(savedInvoice.id ?: -1)

        // then
        assertThat(actual).isNotNull
        assertThat(actual.id).isNotNull
        assertThat(actual.total).isEqualTo(savedInvoice.total)
        assertThat(actual.createdAt).isBefore(LocalDateTime.now())
    }

    @Test
    fun `should not get non-existing invoice `() {

        // when
        val exception = assertThrows<ResourceNotFoundException> {
            service.getInvoiceById(-15)
        }

        // then
        assertThat(exception.message).contains("The invoice with id:", "doesn't exist!")
    }

    @Test
    fun `should get invoice by order id`() {

        // given
        val savedInvoice = transactionTemplate.execute {
            invoiceRepository.save(
                Invoice(
                    orderId = 1L,
                    total = BigDecimal("90.00"),
                    productItems = setOf(
                        ProductItem(
                            name = "test",
                            price = BigDecimal("30"),
                            quantity = 3
                        )
                    )
                )
            )
        } ?: fail("result is expected")

        // when
        val actual = service.getInvoiceByOrderId(savedInvoice.orderId ?: -1)

        // then
        assertThat(actual).isNotNull
        assertThat(actual.id).isEqualTo(savedInvoice.id)
        assertThat(actual.orderId).isEqualTo(savedInvoice.orderId)
        assertThat(actual.total).isEqualTo(savedInvoice.total)
    }

    @Test
    fun `should not get invoice with non-existing order id`() {

        // when
        val exception = assertThrows<ResourceNotFoundException> {
            service.getInvoiceByOrderId(-15)
        }

        // then
        assertThat(exception.message).contains("The invoice with id:", "doesn't exist!")
    }

    @Test
    fun `should refund invoice`() {

        // given
        val savedInvoice = transactionTemplate.execute {
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
        val actual = service.refundInvoice(savedInvoice.id ?: -1)

        // then
        assertThat(actual.id).isEqualTo(savedInvoice.id ?: -1)
        assertThat(actual.status).isEqualTo(InvoiceStatusDTO.REFUND)
    }

    @Test
    fun `should not refund non-paid invoice`() {

        // given
        val savedInvoice = transactionTemplate.execute {
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
                    )
                )
            )
        } ?: fail("result is expected")

        // when
        val exception = assertThrows<NotPaidInvoiceException> {
            service.refundInvoice(savedInvoice.id ?: -1)
        }

        // then
        assertThat(exception.message).contains("The invoice with id", "is not paid")
    }

    @Test
    fun `should pay invoice`() {

        // given
        val savedInvoice = transactionTemplate.execute {
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
        val actual = service.payInvoice(savedInvoice.id ?: -1)

        // then
        assertThat(actual.id).isEqualTo(savedInvoice.id)
        assertThat(actual.status).isEqualTo(InvoiceStatusDTO.PAID)
    }

    @Test
    fun `should not pay invoice without status created`() {

        // given
        val savedInvoice = transactionTemplate.execute {
            invoiceRepository.save(
                Invoice(
                    orderId = 1L,
                    total = BigDecimal("90.00"),
                    status = InvoiceStatus.CANCELLED,
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

        // when
        val exception = assertThrows<InvalidStatusOfInvoice> {
            service.payInvoice(savedInvoice.id ?: -1)
        }

        // then
        assertThat(exception.message)
            .contains("The invoice status should be CREATED to be paid, but status found is invalid")
    }

    @Test
    fun `should cancel invoice by id`() {

        // setup
        wireMockServerStoreClient.start()

        // given
        transactionTemplate.execute {
            invoiceRepository.deleteAll()
        }

        // and
        val savedInvoice = transactionTemplate.execute {
            invoiceRepository.save(
                Invoice(
                    orderId = 1L,
                    total = BigDecimal("90.00"),
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
                                    UpdateStoreRequest(1L, 3)
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
        val actual = service.cancelInvoice(savedInvoice.id ?: -1)

        // then
        assertThat(actual).isNotNull
        assertThat(actual.id).isNotNull
        assertThat(actual.status).isEqualTo(InvoiceStatus.CANCELLED.toDTO())
        assertThat(actual.createdAt).isBefore(LocalDateTime.now())

        // and
        wireMockServerStoreClient.stop()
    }

    @Test
    fun `should cancel order with order status and createdAt less than`() {

        // setup
        wireMockServerStoreClient.start()

        // given
        transactionTemplate.execute {
            invoiceRepository.deleteAll()
        }

        // and
        val savedInvoice = transactionTemplate.execute {
            invoiceRepository.save(
                Invoice(
                    orderId = 1,
                    total = BigDecimal("90.00"),
                    productItems = setOf(
                        ProductItem(
                            priceItemId = 1L,
                            name = "test",
                            price = BigDecimal("30"),
                            quantity = 3
                        )
                    ),
                    status = InvoiceStatus.CREATED
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
                quantity = 5
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
                                    UpdateStoreRequest(1L, 3)
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
        val expiredInvoice = service.cancelExpiredInvoice(LocalDateTime.now().plusDays(10L))

        // then
        assertThat(expiredInvoice[0].status).isEqualTo(InvoiceStatusDTO.CANCELLED)

        wireMockServerStoreClient.stop()
    }

    @Test
    fun `should not cancel non-existing invoice`() {

        // when
        val exception = assertThrows<ResourceNotFoundException> {
            service.cancelInvoice(-15)
        }

        // then
        assertThat(exception.message).contains("Not found invoice with this id")
    }

    @Test
    fun `should not cancel paid invoice`() {

        // given
        val savedInvoice = transactionTemplate.execute {
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

        // when
        val exception = assertThrows<OrderAlreadyHaveInvoice> {
            service.cancelInvoice(savedInvoice.id ?: -1)
        }

        // then
        assertThat(exception.message).contains("This order is already paid. Please, first do refund!")
    }

    @Test
    fun `should update price item`() {

        // given
        val priceItem = PriceItemRequest(
            productId = 1L,
            price = BigDecimal("25.50"),
        )

        // and
        val saved = transactionTemplate.execute {
            priceItemRepository.save(priceItem.toEntity()).toPriceItemResponse()
        } ?: fail("result is expected")

        // and
        val updatedProductRequest = PriceItemRequest(
            productId = 1L,
            price = BigDecimal("35.50"),
        )

        // when
        val actual = service.updatePriceItem(saved.id, updatedProductRequest)

        // then
        assertThat(actual).isNotNull
        assertThat(updatedProductRequest.price).isEqualTo(actual.price)
        assertThat(actual.createdAt).isBeforeOrEqualTo(actual.updatedAt)
    }

    @Test
    fun `should not update not existing price item`() {

        // given
        val id = Long.MAX_VALUE

        // and
        val priceItemRequest = PriceItemRequest(
            productId = 1L,
            price = BigDecimal("25.50"),
        )

        // when
        val exception = assertThrows<ResourceNotFoundException> {
            service.updatePriceItem(Long.MAX_VALUE, priceItemRequest)
        }

        // then
        assertThat(exception.message).isEqualTo("This price item with id: $id doesn't exist!")
    }

    @Test
    fun `should get products price`() {

        // given
        val priceItem = PriceItemRequest(
            productId = 1L,
            price = BigDecimal("25.50"),
        )

        // and
        val saved = transactionTemplate.execute {
            priceItemRepository.save(priceItem.toEntity()).toPriceItemResponse()
        } ?: fail("result is expected")

        // when
        val actual = service.getProductsPrice()

        // then
        assertThat(actual).isNotNull
        assertThat(saved.price).isEqualTo(actual[0].price)
    }

    @Test
    fun `should get products price by products ids`() {

        // given
        transactionTemplate.execute {
            priceItemRepository.deleteAll()
        }

        // and
        val saved = transactionTemplate.execute {
            priceItemRepository.saveAll(
                listOf(
                    PriceItem(
                        productId = 1L,
                        price = BigDecimal("25.50"),
                        markup = BigDecimal.ZERO
                    ),
                    PriceItem(
                        productId = 2L,
                        price = BigDecimal("35.50"),
                        markup = BigDecimal.ZERO
                    ),
                )
            )
        } ?: fail("result is expected")

        // when
        val actual = service.getProductsPriceByProductIds(listOf(1L, 2L), true)

        // then
        assertThat(actual).isNotNull
        assertThat(actual.size).isEqualTo(2)
        assertThat(actual[0].price).isEqualTo(BigDecimal("25.50"))
        assertThat(actual[1].price).isEqualTo(BigDecimal("35.50"))
    }

    @Test
    fun `should get price items by ids`() {

        // given
        transactionTemplate.execute {
            priceItemRepository.deleteAllInBatch()
        }

        // given
        val saved = transactionTemplate.execute {
            priceItemRepository.saveAll(
                listOf(
                    PriceItem(
                        productId = 1L,
                        price = BigDecimal("25.50"),
                        markup = BigDecimal.ZERO
                    ),
                    PriceItem(
                        productId = 2L,
                        price = BigDecimal("35.50"),
                        markup = BigDecimal.ZERO
                    ),
                    PriceItem(
                        productId = 3L,
                        price = BigDecimal("45.50"),
                        markup = BigDecimal.ZERO
                    )
                )
            )
        } ?: fail("result is expected")

        val ids = saved.map { it.id }

        // when
        val actual = service.getPriceItemsByIds(ids as List<Long>, true)

        // then
        assertThat(actual).isNotNull
        assertThat(actual.size).isEqualTo(3)
        assertThat(actual[0].price).isEqualTo(BigDecimal("25.50"))
        assertThat(actual[1].price).isEqualTo(BigDecimal("35.50"))
        assertThat(actual[2].price).isEqualTo(BigDecimal("45.50"))
    }

    @Test
    fun `should get price items by ids with markup`() {

        // given
        transactionTemplate.execute {
            priceItemRepository.deleteAllInBatch()
        }

        // given
        val saved = transactionTemplate.execute {
            priceItemRepository.saveAll(
                listOf(
                    PriceItem(
                        productId = 1L,
                        price = BigDecimal("25.50"),
                        markup = BigDecimal(0.20)
                    ),
                    PriceItem(
                        productId = 2L,
                        price = BigDecimal("35.50"),
                        markup = BigDecimal(0.30)
                    ),
                    PriceItem(
                        productId = 3L,
                        price = BigDecimal("45.50"),
                        markup = BigDecimal(0.20)
                    )
                )
            )
        } ?: fail("result is expected")

        val ids = saved.map { it.id }

        // when
        val actual = service.getPriceItemsByIds(ids as List<Long>, true)

        // then
        assertThat(actual).isNotNull
        assertThat(actual.size).isEqualTo(3)
        assertThat(actual[0].price).isEqualTo(BigDecimal("30.60"))
        assertThat(actual[1].price).isEqualTo(BigDecimal("46.15"))
        assertThat(actual[2].price).isEqualTo(BigDecimal("54.60"))
    }

    @Test
    fun `should update markups in price items`() {

        // given
        transactionTemplate.execute {
            priceItemRepository.deleteAllInBatch()
        }

        // given
        val saved = transactionTemplate.execute {
            priceItemRepository.saveAll(
                listOf(
                    PriceItem(
                        productId = 1L,
                        price = BigDecimal("25.50"),
                        markup = BigDecimal(0.10)
                    ),
                    PriceItem(
                        productId = 2L,
                        price = BigDecimal("25.50"),
                        markup = BigDecimal(0.05)
                    )
                )
            )
        } ?: fail("result is expected")

        // and
        val markupUpdateRequests = listOf(
            MarkupUpdateRequest(saved[0].id ?: -1, BigDecimal(0.20)),
            MarkupUpdateRequest(saved[1].id ?: -1, BigDecimal(0.20)),
        )

        // when
        val actual = service.updateMarkups(markupUpdateRequests)

        // then
        assertThat(actual).isNotNull
        assertThat(actual.size).isEqualTo(2)
        assertThat(actual[0].markup).isEqualTo(BigDecimal("0.20"))
        assertThat(actual[1].markup).isEqualTo(BigDecimal("0.20"))
    }

    @Test
    fun `should create purchased costs`() {

        wireMockServerStoreClient.start()

        // given
        transactionTemplate.execute {
            priceItemRepository.deleteAll()
        }

        // and
        transactionTemplate.execute {
            purchasedCostsRepository.deleteAll()
        }

        // and
        val priceItem = transactionTemplate.execute {
            priceItemRepository.save(
                PriceItem(
                    productId = 1,
                    price = BigDecimal("25.50"),
                    markup = BigDecimal.ZERO
                )
            )
        } ?: fail("result is expected")

        // and
        val purchasedCostsRequest = PurchasedCostsRequest(
            priceItemId = priceItem.id ?: -1,
            quantity = 10,
        )

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

        // when
        val actual = service.createPurchasedCosts(purchasedCostsRequest)

        // then
        assertThat(actual).isNotNull
        assertThat(actual.id).isNotNull
        assertThat(actual.quantity).isEqualTo(10)
        assertThat(actual.priceItemId).isEqualTo(priceItem.id)
        assertThat(actual.dateOfPurchase).isBeforeOrEqualTo(LocalDateTime.now())

        wireMockServerStoreClient.stop()
    }

    @Test
    fun `should not create purchased costs with not existing price item`() {

        // given
        val purchasedCostsRequest = PurchasedCostsRequest(
            priceItemId = -1,
            quantity = 10,
        )

        // when
        val exception = assertThrows<PriceItemNotFoundException> {
            service.createPurchasedCosts(purchasedCostsRequest)
        }

        // then
        assertThat(exception.message).contains("Price Item with id", "was not found")
    }

    @Test
    fun `should get purchased items`() {

        // given
        transactionTemplate.execute {
            invoiceRepository.deleteAll()
        }

        // and
        val created = transactionTemplate.execute {
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
        val actual = service.getPastPurchasedItems()

        // then
        assertThat(actual).hasSize(2)
        assertThat(actual[0].quantity).isEqualTo(10)
        assertThat(actual[1].quantity).isEqualTo(2)
    }
}
