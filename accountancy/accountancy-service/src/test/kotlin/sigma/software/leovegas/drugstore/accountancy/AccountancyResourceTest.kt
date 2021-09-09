package sigma.software.leovegas.drugstore.accountancy

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.matching.ContainsPattern
import com.github.tomakehurst.wiremock.matching.EqualToPattern
import java.math.BigDecimal
import org.assertj.core.api.Assertions.assertThat
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
import sigma.software.leovegas.drugstore.accountancy.api.ConfirmOrderResponse
import sigma.software.leovegas.drugstore.accountancy.api.CreateIncomeInvoiceRequest
import sigma.software.leovegas.drugstore.accountancy.api.CreateOutcomeInvoiceRequest
import sigma.software.leovegas.drugstore.accountancy.api.ItemDTO
import sigma.software.leovegas.drugstore.accountancy.api.ProductItemDtoRequest
import sigma.software.leovegas.drugstore.accountancy.client.AccountancyProperties
import sigma.software.leovegas.drugstore.extensions.get
import sigma.software.leovegas.drugstore.extensions.respTypeRef
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
            )
        )

        val productsDetails = listOf(
            ProductDetailsResponse(
                id = 1L,
                name = "test1",
                price = BigDecimal("20.00"),
                quantity = 3,
            )
        )

        stubFor(
            WireMock.get("/api/v1/products/details?ids=${invoiceRequest[0].productId}")
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

        // and
        stubFor(
            WireMock.get("/api/v1/products/1/price")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(
                    aResponse()
                        .withBody(
                            objectMapper
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(
                                    mapOf(Pair(1, BigDecimal("40.00")))
                                )
                        )
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
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
            respTypeRef<ConfirmOrderResponse>()
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)

        // and
        val body = response.body.get("body")
        assertThat(body.amount).isEqualTo(BigDecimal("80.00"))
    }

    @Test
    fun `should create income invoice`() {

        // given
        transactionalTemplate.execute { invoiceRepository.deleteAll() }

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

        // and
        val productsToCreate = listOf(
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
                            .writeValueAsString(productsToCreate)
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

        val httpEntity = HttpEntity(invoiceRequest)

        // when
        val response = restTemplate.exchange(
            "$baseUrl/api/v1/accountancy/invoice/income",
            POST,
            httpEntity,
            respTypeRef<ConfirmOrderResponse>()
        )

        // then
        val body = response.body.get("body")
        assertThat(body.amount).isEqualTo(BigDecimal("60.00"))
    }

    @Test
    fun `should get invoice by id`() {

        // given
        val savedInvoice = transactionalTemplate.execute {
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
        }.get()

        // when
        val response = restTemplate.exchange(
            "$baseUrl/api/v1/accountancy/invoice/${savedInvoice.id}",
            GET,
            null,
            respTypeRef<ConfirmOrderResponse>()
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        // and
        val body = response.body.get("body")
        assertThat(body.amount).isEqualTo(savedInvoice.total)
        assertThat(body.orderId).isEqualTo(savedInvoice.orderId)
    }

    @Test
    fun `should get invoice details by order id`() {

        // given
        transactionalTemplate.execute { invoiceRepository.deleteAll() }

        // and
        val savedInvoice = transactionalTemplate.execute {
            invoiceRepository.save(
                Invoice(
                    status = InvoiceStatus.PAID,
                    type = InvoiceType.OUTCOME,
                    orderId = 1L,
                    total = BigDecimal("90.00"),
                    productItems = setOf(
                        ProductItem(
                            productId = 1,
                            quantity = 3
                        )
                    )
                )
            )
        }.get()

        // when
        val response = restTemplate.exchange(
            "$baseUrl/api/v1/accountancy/invoice/details/order-id/${savedInvoice.orderId}",
            GET,
            null,
            respTypeRef<List<ItemDTO>>()
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        // and
        val body = response.body.get("body")
        assertThat(body[0].productId).isEqualTo(savedInvoice.productItems.iterator().next().productId)
        assertThat(body[0].quantity).isEqualTo(savedInvoice.productItems.iterator().next().quantity)
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
        }.get()

        stubFor(
            WireMock.get("/api/v1/store/check-transfer/${savedInvoice.orderId}")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(
                    aResponse()
                        .withBody(
                            objectMapper
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(savedInvoice.orderId)
                        )
                        .withStatus(HttpStatus.OK.value())
                )
        )

        // when
        val response = restTemplate.exchange(
            "$baseUrl/api/v1/accountancy/invoice/refund/${savedInvoice.id}",
            PUT,
            null,
            respTypeRef<ConfirmOrderResponse>()
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.ACCEPTED)

        // and
        val body = response.body.get("body")
        assertThat(body.orderId).isEqualTo(savedInvoice.orderId)
        assertThat(body.amount).isEqualTo(savedInvoice.total)
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
        }.get()

        // and
        val httpEntity = HttpEntity(
            BigDecimal("100.00")
        )

        // when
        val response = restTemplate.exchange(
            "$baseUrl/api/v1/accountancy/invoice/pay/${savedInvoice.id}",
            PUT,
            httpEntity,
            respTypeRef<ConfirmOrderResponse>()
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.ACCEPTED)

        // and
        val body = response.body.get("body")
        assertThat(body.orderId).isEqualTo(savedInvoice.orderId)
        assertThat(body.amount).isEqualTo(savedInvoice.total)
    }

    @Test
    fun `should cancel invoice by id`() {

        // given
        transactionalTemplate.execute {
            invoiceRepository.deleteAll()
        }

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
                            price = BigDecimal("30"),
                            quantity = 3
                        )
                    )
                )
            )
        }.get()

        // when
        val response = restTemplate.exchange(
            "$baseUrl/api/v1/accountancy/invoice/cancel/${savedInvoice.id}",
            PUT,
            null,
            respTypeRef<ConfirmOrderResponse>()
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.ACCEPTED)

        // and
        val body = response.body.get("body")
        assertThat(body.orderId).isEqualTo(savedInvoice.orderId)
        assertThat(body.amount).isEqualTo(savedInvoice.total)
    }

    @Test
    fun `should get sale price`() {

        // given
        stubFor(
            WireMock.get("/api/v1/products/1,2/price")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(
                    aResponse()
                        .withBody(
                            objectMapper
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(
                                    mapOf(Pair(1, BigDecimal("1.23")), Pair(2, BigDecimal("1.24")))
                                )
                        )
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                )
        )

        // when
        val response = restTemplate.exchange(
            "$baseUrl/api/v1/accountancy/sale-price?ids=1&ids=2",
            GET,
            null,
            respTypeRef<Map<Long, BigDecimal>>()
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        // and
        val body = response.body.get("body")
        assertThat(body[1]).isEqualTo(BigDecimal("1.23"))
        assertThat(body[2]).isEqualTo(BigDecimal("1.24"))
    }
}
