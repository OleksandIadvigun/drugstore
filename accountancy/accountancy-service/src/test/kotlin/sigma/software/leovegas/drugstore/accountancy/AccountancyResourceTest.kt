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
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpMethod.POST
import org.springframework.http.HttpMethod.PUT
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.accountancy.api.ConfirmOrderResponse
import sigma.software.leovegas.drugstore.accountancy.api.CreateIncomeInvoiceRequest
import sigma.software.leovegas.drugstore.accountancy.api.CreateOutcomeInvoiceEvent
import sigma.software.leovegas.drugstore.accountancy.api.ItemDTO
import sigma.software.leovegas.drugstore.accountancy.api.ProductItemDtoRequest
import sigma.software.leovegas.drugstore.accountancy.client.AccountancyProperties
import sigma.software.leovegas.drugstore.api.protobuf.Proto
import sigma.software.leovegas.drugstore.api.toDecimalProto
import sigma.software.leovegas.drugstore.extensions.get
import sigma.software.leovegas.drugstore.extensions.respTypeRef
import sigma.software.leovegas.drugstore.extensions.withProtobufResponse
import sigma.software.leovegas.drugstore.product.api.CreateProductRequest
import sigma.software.leovegas.drugstore.product.api.ProductDetailsResponse
import sigma.software.leovegas.drugstore.store.api.CheckStatusResponse

@DisplayName("Accountancy Resource test")
class AccountancyResourceTest @Autowired constructor(
    @LocalServerPort val port: Int,
    val restTemplate: TestRestTemplate,
    val transactionalTemplate: TransactionTemplate,
    val invoiceRepository: InvoiceRepository,
    val accountancyProperties: AccountancyProperties,
    val objectMapper: ObjectMapper
) : WireMockTest() {

    lateinit var baseUrl: String

    @BeforeEach
    fun setup() {
        baseUrl = "http://${accountancyProperties.host}:$port"
    }

    @Test
    fun `should create outcome invoice`() {

        // setup
        transactionalTemplate.execute {
            invoiceRepository.deleteAll()
        }

        // given
        val invoiceRequest = listOf(
            ItemDTO(
                productNumber = "1",
                quantity = 2,
            )
        )

        // and
        val productsProto = listOf(
            Proto.ProductDetailsItem.newBuilder()
                .setName("test1").setProductNumber("1").setQuantity(3)
                .setPrice(BigDecimal("20.00").toDecimalProto())
                .build(),

            )

        // given
        stubFor(
            WireMock.get("/api/v1/products/details?productNumbers=1")
                .willReturn(
                    aResponse()
                        .withProtobufResponse {
                            Proto.ProductDetailsResponse.newBuilder().addAllProducts(productsProto).build()
                        }
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
                                    mapOf(Pair("1", BigDecimal("40.00")))
                                )
                        )
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                )
        )

        // and
        val httpEntity = HttpEntity(
            CreateOutcomeInvoiceEvent(invoiceRequest, "1")
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
        assertThat(body.amount).isEqualTo(BigDecimal("160.00"))
        assertThat(body.orderNumber).isEqualTo("1")
    }

    @Test
    fun `should create income invoice`() {

        // setup
        transactionalTemplate.execute { invoiceRepository.deleteAll() }

        // given
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

        // and
        val productsDetails = listOf(
            ProductDetailsResponse(
                productNumber = "1",
                name = "test1",
                price = BigDecimal("20.00"),
                quantity = 1,
            ),
            ProductDetailsResponse(
                productNumber = "2",
                name = "test2",
                price = BigDecimal("20.00"),
                quantity = 2,
            )
        )

        // and
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

        // and
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
        assertThat(body.orderNumber).isNotEqualTo("undefined")
    }

    @Test
    fun `should get invoice by invoice number`() {

        // setup
        transactionalTemplate.execute { invoiceRepository.deleteAll() }

        // given
        val savedInvoice = transactionalTemplate.execute {
            invoiceRepository.save(
                Invoice(
                    invoiceNumber = "1",
                    orderNumber = "1",
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
            "$baseUrl/api/v1/accountancy/invoice/${savedInvoice.invoiceNumber}",
            GET,
            null,
            respTypeRef<ConfirmOrderResponse>()
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        // and
        val body = response.body.get("body")
        assertThat(body.amount).isEqualTo(savedInvoice.total)
        assertThat(body.orderNumber).isEqualTo(savedInvoice.orderNumber)
    }

    @Test
    fun `should get invoice details by order number`() {

        // setup
        transactionalTemplate.execute {
            invoiceRepository.deleteAll()
        }

        // given
        val savedInvoice = transactionalTemplate.execute {
            invoiceRepository.save(
                Invoice(
                    invoiceNumber = "1",
                    status = InvoiceStatus.PAID,
                    type = InvoiceType.OUTCOME,
                    orderNumber = "1",
                    total = BigDecimal("90.00"),
                    productItems = setOf(
                        ProductItem(
                            productNumber = "1",
                            quantity = 3
                        )
                    )
                )
            )
        }.get()

        // when
        val response = restTemplate.exchange(
            "$baseUrl/api/v1/accountancy/invoice/details/order-number/${savedInvoice.orderNumber}",
            GET,
            null,
            respTypeRef<Proto.InvoiceDetails>()
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        // and
        val body = response.body.get("body")
        assertThat(body.itemsList[0].productNumber).isEqualTo(savedInvoice.productItems.iterator().next().productNumber)
        assertThat(body.itemsList[0].quantity).isEqualTo(savedInvoice.productItems.iterator().next().quantity)
    }

    @Test
    fun `should refund invoice`() {

        // setup
        transactionalTemplate.execute {
            invoiceRepository.deleteAll()
        }

        // given
        val savedInvoice = transactionalTemplate.execute {
            invoiceRepository.save(
                Invoice(
                    invoiceNumber = "1",
                    orderNumber = "1",
                    total = BigDecimal("90.00"),
                    status = InvoiceStatus.PAID,
                    productItems = setOf(
                        ProductItem(
                            productNumber = "1",
                            name = "test",
                            price = BigDecimal("30"),
                            quantity = 3
                        )
                    )
                )
            )
        }.get()

        // and
        stubFor(
            WireMock.get("/api/v1/store/check-transfer/${savedInvoice.orderNumber}")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(
                    aResponse()
                        .withBody(
                            objectMapper
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(CheckStatusResponse(savedInvoice.orderNumber))
                        )
                        .withStatus(HttpStatus.OK.value())
                )
        )

        // when
        val response = restTemplate.exchange(
            "$baseUrl/api/v1/accountancy/invoice/refund/${savedInvoice.orderNumber}",
            PUT,
            null,
            respTypeRef<ConfirmOrderResponse>()
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.ACCEPTED)

        // and
        val body = response.body.get("body")
        assertThat(body.orderNumber).isEqualTo(savedInvoice.orderNumber)
        assertThat(body.amount).isEqualTo(savedInvoice.total)
    }

    @Test
    fun `should pay invoice`() {

        // setup
        transactionalTemplate.execute {
            invoiceRepository.deleteAll()
        }

        // given
        val savedInvoice = transactionalTemplate.execute {
            invoiceRepository.save(
                Invoice(
                    invoiceNumber = "1",
                    orderNumber = "1",
                    total = BigDecimal("90.00"),
                    status = InvoiceStatus.CREATED,
                    productItems = setOf(
                        ProductItem(
                            productNumber = "1",
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
            "$baseUrl/api/v1/accountancy/invoice/pay/${savedInvoice.orderNumber}",
            PUT,
            httpEntity,
            respTypeRef<ConfirmOrderResponse>()
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.ACCEPTED)

        // and
        val body = response.body.get("body")
        assertThat(body.orderNumber).isEqualTo(savedInvoice.orderNumber)
        assertThat(body.amount).isEqualTo(savedInvoice.total)
    }

    @Test
    fun `should cancel invoice by order number`() {

        // setup
        transactionalTemplate.execute {
            invoiceRepository.deleteAll()
        }

        // and
        transactionalTemplate.execute {
            invoiceRepository.deleteAll()
        }

        // given
        val savedInvoice = transactionalTemplate.execute {
            invoiceRepository.save(
                Invoice(
                    invoiceNumber = "1",
                    orderNumber = "1",
                    total = BigDecimal("90.00"),
                    productItems = setOf(
                        ProductItem(
                            productNumber = "1",
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
            "$baseUrl/api/v1/accountancy/invoice/cancel/${savedInvoice.orderNumber}",
            PUT,
            null,
            respTypeRef<ConfirmOrderResponse>()
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.ACCEPTED)

        // and
        val body = response.body.get("body")
        assertThat(body.orderNumber).isEqualTo(savedInvoice.orderNumber)
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
                                    mapOf(Pair("1", BigDecimal("1.23")), Pair("2", BigDecimal("1.24")))
                                )
                        )
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                )
        )

        // when
        val response = restTemplate.exchange(
            "$baseUrl/api/v1/accountancy/sale-price?productNumbers=1&productNumbers=2",
            GET,
            null,
            respTypeRef<Map<String, BigDecimal>>()
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        // and
        val body = response.body.get("body")
        assertThat(body["1"]).isEqualTo(BigDecimal("2.46"))
        assertThat(body["2"]).isEqualTo(BigDecimal("2.48"))
    }
}
