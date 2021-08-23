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
import sigma.software.leovegas.drugstore.accountancy.api.PriceItemRequest
import sigma.software.leovegas.drugstore.accountancy.api.PriceItemResponse
import sigma.software.leovegas.drugstore.order.api.OrderDetailsDTO
import sigma.software.leovegas.drugstore.order.api.OrderItemDetailsDTO
import sigma.software.leovegas.drugstore.order.api.OrderResponse
import sigma.software.leovegas.drugstore.order.api.OrderStatusDTO
import sigma.software.leovegas.drugstore.store.api.StoreResponse
import sigma.software.leovegas.drugstore.store.api.UpdateStoreRequest

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
@AutoConfigureWireMock(port = 8082)
@DisplayName("Accountancy Service test")
class AccountancyServiceTest @Autowired constructor(
    val service: AccountancyService,
    val transactionTemplate: TransactionTemplate,
    val priceItemRepository: PriceItemRepository,
    val invoiceRepository: InvoiceRepository,
    val objectMapper: ObjectMapper
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
        wireMockServerStoreClient.start();

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
        assertThat(actual.createdAt).isBefore(LocalDateTime.now())

        // and
        wireMockServerStoreClient.stop()
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
    fun `should cancel invoice by id`() {

        // given
        wireMockServerStoreClient.start();

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
        wireMockServerStoreClient.stop();
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
        assertThat(actual.createdAt).isBefore(actual.updatedAt)
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
        assertThat(saved.price).isEqualTo(actual[1])
    }

    @Test
    fun `should get products price by products ids`() {

        // given
        val saved = transactionTemplate.execute {
            priceItemRepository.saveAll(
                listOf(
                    PriceItem(
                        productId = 1L,
                        price = BigDecimal("25.50")
                    ),
                    PriceItem(
                        productId = 2L,
                        price = BigDecimal("35.50")
                    ),
                )
            )
        } ?: fail("result is expected")

        // when
        val actual = service.getProductsPriceByProductIds(listOf(1L, 2L))

        // then
        assertThat(actual).isNotNull
        assertThat(actual.size).isEqualTo(2)
        assertThat(saved[0].price).isEqualTo(actual[1])
        assertThat(saved[1].price).isEqualTo(actual[2])
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
                        price = BigDecimal("25.50")
                    ),
                    PriceItem(
                        productId = 2L,
                        price = BigDecimal("35.50")
                    ),
                    PriceItem(
                        productId = 3L,
                        price = BigDecimal("45.50")
                    )
                )
            )
        } ?: fail("result is expected")

        val ids = saved.map { it.id }

        // when
        val actual = service.getPriceItemsByIds(ids as List<Long>)

        // then
        assertThat(actual).isNotNull
        assertThat(actual.size).isEqualTo(3)
        assertThat(actual[0].price).isEqualTo(BigDecimal("25.50"))
        assertThat(actual[1].price).isEqualTo(BigDecimal("35.50"))
        assertThat(actual[2].price).isEqualTo(BigDecimal("45.50"))
    }
}
