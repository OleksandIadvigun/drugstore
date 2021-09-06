package sigma.software.leovegas.drugstore.accountancy

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock
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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
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
import sigma.software.leovegas.drugstore.accountancy.api.CreateIncomeInvoiceRequest
import sigma.software.leovegas.drugstore.accountancy.api.CreateOutcomeInvoiceRequest
import sigma.software.leovegas.drugstore.accountancy.api.InvoiceResponse
import sigma.software.leovegas.drugstore.accountancy.api.InvoiceStatusDTO
import sigma.software.leovegas.drugstore.accountancy.api.InvoiceTypeDTO
import sigma.software.leovegas.drugstore.accountancy.api.ItemDTO
import sigma.software.leovegas.drugstore.accountancy.api.ProductItemDtoRequest
import sigma.software.leovegas.drugstore.accountancy.client.AccountancyProperties
import sigma.software.leovegas.drugstore.extensions.respTypeRef
import sigma.software.leovegas.drugstore.order.api.OrderResponse
import sigma.software.leovegas.drugstore.order.api.OrderStatusDTO
import sigma.software.leovegas.drugstore.product.api.CreateProductRequest
import sigma.software.leovegas.drugstore.product.api.ProductDetailsResponse

@DisplayName("Accountancy Resource test")
@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureWireMock(port = 8079)
class AccountancyResourceTest @Autowired constructor(
    @LocalServerPort val port: Int,
    val restTemplate: TestRestTemplate,
    val transactionalTemplate: TransactionTemplate,
    val invoiceRepository: InvoiceRepository,
    val accountancyProperties: AccountancyProperties,
    val objectMapper: ObjectMapper
) {

    lateinit var baseUrl: String

    @BeforeEach
    fun setup() {
        baseUrl = "http://${accountancyProperties.host}:$port"
    }

    @Test
    fun `should create outcome invoice`() {

        // given
        transactionalTemplate.execute {
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
                    WireMock.aResponse()
                        .withBody(
                            objectMapper
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(productsDetails)
                        )
                        .withStatus(HttpStatus.OK.value())
                )
        )

        val httpEntity = HttpEntity(
            CreateOutcomeInvoiceRequest(invoiceRequest, 1L)
        )

        // when
        val response = restTemplate.exchange(
            "$baseUrl/api/v1/accountancy/invoice/outcome",
            POST,
            httpEntity,
            respTypeRef<InvoiceResponse>()
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)

        // and
        val body = response.body ?: fail("body may not be null")
        assertThat(body.id).isNotNull
        assertThat(body.total).isEqualTo(BigDecimal("60.00"))
        assertThat(body.createdAt).isBefore(LocalDateTime.now())
        assertThat(body.type).isEqualTo(InvoiceTypeDTO.OUTCOME)
        assertThat(body.status).isEqualTo(InvoiceStatusDTO.CREATED)
    }

    @Test
    fun `should create income invoice`() {

        // given
        transactionalTemplate.execute {
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

        val httpEntity = HttpEntity(
            invoiceRequest
        )

        // when
        val response = restTemplate.exchange(
            "$baseUrl/api/v1/accountancy/invoice/income",
            POST,
            httpEntity,
            respTypeRef<InvoiceResponse>()
        )

        // then
        val body = response.body ?: fail("body may not be null")
        assertThat(body.id).isNotNull
        assertThat(body.total).isEqualTo(BigDecimal("60.00"))
        assertThat(body.createdAt).isBefore(LocalDateTime.now())
        assertThat(body.type).isEqualTo(InvoiceTypeDTO.INCOME)
        assertThat(body.status).isEqualTo(InvoiceStatusDTO.CREATED)
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

        // and
        val httpEntity = HttpEntity(
            BigDecimal("100.00")
        )
        // when
        val response = restTemplate.exchange(
            "$baseUrl/api/v1/accountancy/invoice/pay/${savedInvoice.id}",
            PUT,
            httpEntity,
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

        // given
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
                            productId = 1L,
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
    }
}
