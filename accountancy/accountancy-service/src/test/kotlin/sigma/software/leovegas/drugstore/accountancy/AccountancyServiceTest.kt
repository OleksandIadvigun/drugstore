package sigma.software.leovegas.drugstore.accountancy

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.put
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.matching.ContainsPattern
import com.github.tomakehurst.wiremock.matching.EqualToPattern
import java.math.BigDecimal
import java.time.LocalDateTime
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.fail
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.accountancy.api.CreateIncomeInvoiceRequest
import sigma.software.leovegas.drugstore.accountancy.api.CreateOutcomeInvoiceRequest
import sigma.software.leovegas.drugstore.accountancy.api.InvoiceStatusDTO
import sigma.software.leovegas.drugstore.accountancy.api.InvoiceTypeDTO
import sigma.software.leovegas.drugstore.accountancy.api.ItemDTO
import sigma.software.leovegas.drugstore.accountancy.api.ProductItemDtoRequest
import sigma.software.leovegas.drugstore.order.api.OrderResponse
import sigma.software.leovegas.drugstore.order.api.OrderStatusDTO
import sigma.software.leovegas.drugstore.product.api.CreateProductRequest
import sigma.software.leovegas.drugstore.product.api.ProductDetailsResponse

@AutoConfigureTestDatabase
@AutoConfigureWireMock(port = 8079)
@DisplayName("Accountancy Service test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AccountancyServiceTest @Autowired constructor(
    val service: AccountancyService,
    val transactionTemplate: TransactionTemplate,
    val invoiceRepository: InvoiceRepository,
    val objectMapper: ObjectMapper,
) {

    @Test
    fun `should create outcome invoice`() {

        // given
        transactionTemplate.execute {
            invoiceRepository.deleteAll()
        }

        // and
        val invoiceRequest = listOf(
            ItemDTO(
                productId = 1L,
                quantity = 2,
            ),
            ItemDTO(
                productId = 2L,
                quantity = 2,
            )
        )

        val productsDetails = listOf(
            ProductDetailsResponse(
                id = 1L,
                name = "test1",
                price = BigDecimal("20.00"),
                quantity = 3,
            ),
            ProductDetailsResponse(
                id = 2L,
                name = "test2",
                price = BigDecimal("10.00"),
                quantity = 3,
            )
        )

        stubFor(
            get("/api/v1/products/details?ids=${invoiceRequest[0].productId}&ids=${invoiceRequest[1].productId}")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(
                    aResponse()
                        .withBody(
                            objectMapper
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(productsDetails)
                        )
                        .withStatus(HttpStatus.OK.value())
                )
        )

        // when
        val actual = service.createOutcomeInvoice(CreateOutcomeInvoiceRequest(invoiceRequest, orderId = 1L))

        // then
        assertThat(actual).isNotNull
        assertThat(actual.id).isNotNull
        assertThat(actual.total).isEqualTo(BigDecimal("60.00")) // sum of all quantity * price
        assertThat(actual.createdAt).isBeforeOrEqualTo(LocalDateTime.now())
        assertThat(actual.type).isEqualTo(InvoiceTypeDTO.OUTCOME)
        assertThat(actual.status).isEqualTo(InvoiceStatusDTO.CREATED)

    }

    @Test
    fun `should create income invoice`() {

        // given
        transactionTemplate.execute {
            invoiceRepository.deleteAll()
        }

        // and
        val invoiceRequest = CreateIncomeInvoiceRequest(
            productItems = listOf(
                ProductItemDtoRequest(
                    name = "test1",
                    quantity = 1,
                    price = BigDecimal("20.00")
                ),
                ProductItemDtoRequest(
                    name = "test2",
                    quantity = 2,
                    price = BigDecimal("20.00")
                )
            )
        )

        val productsDetails = listOf(
            ProductDetailsResponse(
                id = 1L,
                name = "test1",
                price = BigDecimal("20.00"),
                quantity = 1,
            ),
            ProductDetailsResponse(
                id = 2L,
                name = "test2",
                price = BigDecimal("20.00"),
                quantity = 2,
            )
        )

        stubFor(
            post("/api/v1/products")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
                .withRequestBody(
                    EqualToPattern(
                        objectMapper
                            .writerWithDefaultPrettyPrinter()
                            .writeValueAsString(
                                listOf(
                                    CreateProductRequest(
                                        name = "test1",
                                        quantity = 1,
                                        price = BigDecimal("20.00")
                                    ),
                                    CreateProductRequest(
                                        name = "test2",
                                        quantity = 2,
                                        price = BigDecimal("20.00")
                                    )
                                )
                            )
                    )
                )
                .willReturn(
                    aResponse()
                        .withBody(
                            objectMapper
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(productsDetails)
                        )
                        .withStatus(HttpStatus.OK.value())
                )
        )

        // when
        val actual = service.createIncomeInvoice(invoiceRequest)

        // then
        assertThat(actual).isNotNull
        assertThat(actual.id).isNotNull
        assertThat(actual.total).isEqualTo(BigDecimal("60.00")) // sum of all quantity * price
        assertThat(actual.createdAt).isBeforeOrEqualTo(LocalDateTime.now())
        assertThat(actual.type).isEqualTo(InvoiceTypeDTO.INCOME)
        assertThat(actual.status).isEqualTo(InvoiceStatusDTO.CREATED)
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
        val invoiceRequest = listOf(
            ItemDTO()
        )

        // when
        val exception = assertThrows<OrderAlreadyHaveInvoice> {
            service.createOutcomeInvoice(CreateOutcomeInvoiceRequest(invoiceRequest, 1L))
        }

        // then
        assertThat(exception.message).contains("This order already has some invoice")
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
        assertThat(actual.createdAt).isBeforeOrEqualTo(LocalDateTime.now())
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
                            productId = 1L,
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
                            productId = 1L,
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
                            productId = 1L,
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
        val actual = service.payInvoice(savedInvoice.id ?: -1, BigDecimal("100.0"))

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
                            productId = 1L,
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
            service.payInvoice(savedInvoice.id ?: -1, BigDecimal("100.00"))
        }

        // then
        assertThat(exception.message)
            .contains("The invoice status should be CREATED to be paid, but status found is invalid")
    }

    @Test
    fun `should not pay invoice if not enough money`() {

        // given
        val savedInvoice = transactionTemplate.execute {
            invoiceRepository.save(
                Invoice(
                    orderId = 1L,
                    total = BigDecimal("90.00"),
                    status = InvoiceStatus.CREATED,
                    productItems = setOf(
                        ProductItem(
                            productId = 1L,
                            name = "test",
                            price = BigDecimal("30"),
                            quantity = 3
                        )
                    ),
                )
            )
        } ?: fail("result is expected")

        // when
        val exception = assertThrows<NotEnoughMoneyException> {
            service.payInvoice(savedInvoice.id ?: -1, BigDecimal("10.00"))
        }

        // then
        assertThat(exception.message)
            .contains("Not enough money for this transaction!")
    }

    @Test
    fun `should cancel invoice by id`() {

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
                            productId = 1L,
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
        stubFor(
            post("/api/v1/store/return")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
                .withRequestBody(
                    EqualToPattern(
                        objectMapper
                            .writerWithDefaultPrettyPrinter()
                            .writeValueAsString(savedInvoice.id)
                    )
                )
                .willReturn(
                    aResponse()
                        .withBody(
                            objectMapper
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(OrderResponse(orderStatus = OrderStatusDTO.REFUND)) //todo
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
        assertThat(actual.createdAt).isBeforeOrEqualTo(LocalDateTime.now())
    }

    @Test
    fun `should cancel invoice with order status and createdAt less than`() {

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
                            productId = 1L,
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
        stubFor(
            post("/api/v1/store/return")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
                .withRequestBody(
                    EqualToPattern(
                        objectMapper
                            .writerWithDefaultPrettyPrinter()
                            .writeValueAsString(savedInvoice.id)
                    )
                )
                .willReturn(
                    aResponse()
                        .withBody(
                            objectMapper
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(OrderResponse(orderStatus = OrderStatusDTO.REFUND)) //todo
                        )
                        .withStatus(HttpStatus.OK.value())
                )
        )

        // when
        val expiredInvoice = service.cancelExpiredInvoice(LocalDateTime.now().plusDays(10L))

        // then
        assertThat(expiredInvoice[0].status).isEqualTo(InvoiceStatusDTO.CANCELLED)
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
                            productId = 1L,
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
}
