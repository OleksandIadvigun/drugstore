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
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.accountancy.api.CreateIncomeInvoiceRequest
import sigma.software.leovegas.drugstore.accountancy.api.CreateOutcomeInvoiceEvent
import sigma.software.leovegas.drugstore.accountancy.api.ItemDTO
import sigma.software.leovegas.drugstore.accountancy.api.ProductItemDtoRequest
import sigma.software.leovegas.drugstore.api.protobuf.Proto
import sigma.software.leovegas.drugstore.api.toDecimalProto
import sigma.software.leovegas.drugstore.extensions.get
import sigma.software.leovegas.drugstore.extensions.withProtobufResponse
import sigma.software.leovegas.drugstore.product.api.CreateProductRequest
import sigma.software.leovegas.drugstore.product.api.ProductDetailsResponse

@DisplayName("Accountancy Service test")
class AccountancyServiceTest @Autowired constructor(
    val service: AccountancyService,
    val transactionTemplate: TransactionTemplate,
    val invoiceRepository: InvoiceRepository,
    val objectMapper: ObjectMapper,
) : WireMockTest() {

    @Test
    fun `should create outcome invoice`() {

        // setup
        transactionTemplate.execute {
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

        val orderNumber = "1"

        // when
        val actual = service.createOutcomeInvoice(CreateOutcomeInvoiceEvent(invoiceRequest, orderNumber))

        // then
        assertThat(actual).isNotNull
        assertThat(actual.orderNumber).isEqualTo(orderNumber)
        assertThat(actual.amount).isEqualTo(BigDecimal("160.00")) // sum of all quantity * price
    }

    @Test
    fun `should create income invoice`() {

        // setup
        transactionTemplate.execute {
            invoiceRepository.deleteAll()
        }

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

        // when
        val actual = service.createIncomeInvoice(invoiceRequest)

        // then
        assertThat(actual).isNotNull
        assertThat(actual.amount).isEqualTo(BigDecimal("60.00")) // sum of all quantity * price
    }

//    @Test           // todo
//    fun `should not create invoice with order id already in the another invoice`() {
//
//        // given
//        transactionTemplate.execute {
//            invoiceRepository.deleteAll()
//        }
//
//        // given
//        val savedInvoice = transactionTemplate.execute {
//            invoiceRepository.save(
//                Invoice(
//                    orderNumber = 1,
//                    total = BigDecimal("90.00"),
//                    productItems = setOf(
//                        ProductItem(
//                            name = "test",
//                            price = BigDecimal("30"),
//                            quantity = 3
//                        )
//                    )
//                )
//            )
//        }.get()
//
//        // and
//        val invoiceRequest = listOf(ItemDTO(productId = savedInvoice.productItems.iterator().next().id ?: -1, quantity = 3))
//
//        // when
//        val exception = assertThrows<OrderAlreadyConfirmedException> {
//            service.createOutcomeInvoice(CreateOutcomeInvoiceRequest(invoiceRequest, savedInvoice.orderNumber))
//        }
//
//        // then
//        assertThat(exception.message).contains("Order(${savedInvoice.orderNumber}) already has invoice")
//    }

    @Test
    fun `should get invoice by invoice number`() {

        // given
        val savedInvoice = transactionTemplate
            .execute {
                invoiceRepository.save(
                    Invoice(
                        invoiceNumber = "1",
                        orderNumber = "1",
                        status = InvoiceStatus.CREATED,
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
        val actual = service.getInvoiceByInvoiceNumber(savedInvoice.invoiceNumber)

        // then
        assertThat(actual).isNotNull
        assertThat(actual.amount).isEqualTo(savedInvoice.total)
        assertThat(actual.invoiceNumber).isEqualTo("1")
    }

    @Test
    fun `should not get non-existing invoice `() {

        // given
        val invalidInvoiceNumber = "invalid"

        // when
        val exception = assertThrows<InvoiceNotFoundException> {
            service.getInvoiceByInvoiceNumber(invalidInvoiceNumber)
        }

        // then
        assertThat(exception.message).contains("Invoice of Order($invalidInvoiceNumber) not found.")
    }

    @Test
    fun `should get invoice details by order number`() {

        // setup
        transactionTemplate.execute { invoiceRepository.deleteAll() }

        // given
        val savedInvoice = transactionTemplate.execute {
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
        val actual = service.getInvoiceDetailsByOrderNumber(savedInvoice.orderNumber)

        // then
        assertThat(actual).isNotNull
        assertThat(actual.itemsList).hasSize(1)
        assertThat(actual.itemsList[0].productNumber).isEqualTo("1")
        assertThat(actual.itemsList[0].quantity).isEqualTo(3)
    }

    @Test
    fun `should not get invoice with non-existing order number`() {

        // given
        val invalidOrderNumber = "invalid"

        // when
        val exception = assertThrows<InvoiceNotFoundException> {
            service.getInvoiceDetailsByOrderNumber(invalidOrderNumber)
        }

        // then
        assertThat(exception.message).contains("Invoice of Order($invalidOrderNumber) not found.")
    }

    @Test
    fun `should refund invoice`() {

        // setup
        transactionTemplate.execute {
            invoiceRepository.deleteAll()
        }

        // given
        val savedInvoice = transactionTemplate.execute {
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

        val responseExpected =
            Proto.CheckTransferResponse.newBuilder().setOrderNumber(savedInvoice.orderNumber)
                .setComment("Not delivered").build()

        // and
        stubFor(
            WireMock.get("/api/v1/store/check-transfer/${savedInvoice.orderNumber}")
                .willReturn(
                    aResponse()
                        .withProtobufResponse { responseExpected }
                        .withStatus(HttpStatus.OK.value())
                )
        )

        // when
        val actual = service.refundInvoice(savedInvoice.orderNumber)

        // then
        assertThat(actual.orderNumber).isEqualTo(savedInvoice.orderNumber)
        assertThat(actual.amount).isEqualTo(savedInvoice.total)
    }

    @Test
    fun `should not refund if invoice not exist`() {

        // setup
        transactionTemplate.execute {
            invoiceRepository.deleteAll()
        }

        // given
        val invalidOrderNumber = "invalid"

        // when
        val exception = assertThrows<InvoiceNotFoundException> {
            service.refundInvoice(invalidOrderNumber)
        }

        // then
        assertThat(exception.message).contains("Invoice of Order($invalidOrderNumber) not found.")
    }

    @Test
    fun `should not refund if store service not available`() {

        // setup
        transactionTemplate.execute {
            invoiceRepository.deleteAll()
        }

        // given
        val savedInvoice = transactionTemplate.execute {
            invoiceRepository.save(
                Invoice(
                    invoiceNumber = "1",
                    orderNumber = "-5",
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

        // when
        val exception = assertThrows<StoreServiceResponseException> {
            service.refundInvoice(savedInvoice.orderNumber.get())
        }

        // then
        assertThat(exception.message).startsWith(
            "Ups... some problems in store service."
        )
    }


    @Test
    fun `should not refund non-paid invoice`() {

        // setup
        transactionTemplate.execute {
            invoiceRepository.deleteAll()
        }

        // given
        val savedInvoice = transactionTemplate.execute {
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
                    )
                )
            )
        }.get()

        // when
        val exception = assertThrows<NotPaidInvoiceException> {
            service.refundInvoice(savedInvoice.orderNumber)
        }

        // then
        assertThat(exception.message).contains("The invoice with invoice number = ${savedInvoice.invoiceNumber} is not paid")
    }

    @Test
    fun `should pay invoice`() {

        // setup
        transactionTemplate.execute {
            invoiceRepository.deleteAll()
        }

        // given
        val savedInvoice = transactionTemplate.execute {
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

        // when
        val actual = service.payInvoice(savedInvoice.orderNumber, BigDecimal("90.0"))

        // then
        assertThat(actual.orderNumber).isEqualTo(savedInvoice.orderNumber)
        assertThat(actual.amount).isEqualTo(savedInvoice.total)
    }

    @Test
    fun `should not pay if invoice not exist`() {

        // setup
        transactionTemplate.execute {
            invoiceRepository.deleteAll()
        }

        // given
        val invalidOrderNumber = "invalid"

        // when
        val exception = assertThrows<InvoiceNotFoundException> {
            service.payInvoice(invalidOrderNumber, BigDecimal.TEN)
        }

        // then
        assertThat(exception.message).contains("Invoice of Order($invalidOrderNumber) not found.")
    }

    @Test
    fun `should not pay invoice if status not created`() {

        // setup
        transactionTemplate.execute {
            invoiceRepository.deleteAll()
        }

        // given
        val savedInvoice = transactionTemplate.execute {
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
                    ),
                )
            )
        }.get()

        // when
        val exception = assertThrows<InvalidStatusOfInvoice> {
            service.payInvoice(savedInvoice.orderNumber, BigDecimal("100.00"))
        }

        // then
        assertThat(exception.message)
            .contains("The invoice status should be CREATED to be paid, but status found is invalid")
    }

    @Test
    fun `should not pay invoice if not enough money`() {

        // setup
        transactionTemplate.execute {
            invoiceRepository.deleteAll()
        }

        // given
        val savedInvoice = transactionTemplate.execute {
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

        // when
        val exception = assertThrows<NotEnoughMoneyException> {
            service.payInvoice(savedInvoice.orderNumber, BigDecimal("10.00"))
        }

        // then
        assertThat(exception.message).contains("Not enough money for this transaction")
    }

    @Test
    fun `should cancel invoice by id`() {

        // setup
        transactionTemplate.execute {
            invoiceRepository.deleteAll()
        }

        // given
        val savedInvoice = transactionTemplate.execute {
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
        val actual = service.cancelInvoice(savedInvoice.orderNumber)

        // then
        assertThat(actual.orderNumber).isEqualTo(savedInvoice.orderNumber)
        assertThat(actual.amount).isEqualTo(savedInvoice.total)

    }

    @Test
    fun `should not cancel non-existing invoice`() {

        // setup
        transactionTemplate.execute {
            invoiceRepository.deleteAll()
        }

        // given
        val invalidOrderNumber = "invalid"

        // when
        val exception = assertThrows<InvoiceNotFoundException> {
            service.cancelInvoice(invalidOrderNumber)
        }

        // then
        assertThat(exception.message).contains("Invoice of Order($invalidOrderNumber) not found.")
    }

    @Test
    fun `should not cancel paid invoice`() {

        // setup
        transactionTemplate.execute {
            invoiceRepository.deleteAll()
        }

        // given
        val savedInvoice = transactionTemplate.execute {
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
        } get "result"

        // when
        val exception = assertThrows<InvoiceAlreadyPaidException> {
            service.cancelInvoice(savedInvoice.orderNumber)
        }

        // then
        assertThat(exception.message).contains("Order(${savedInvoice.orderNumber}) already paid. Please, first do refund")
    }

    @Test
    fun `should get sale price`() {

        // given
        val productNumbers = listOf("1", "2")

        // and
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
        val priceMap = service.getSalePrice(productNumbers)

        // then
        println(priceMap)
        assertThat(priceMap["1"]).isEqualTo(BigDecimal("1.23").multiply(BigDecimal("2")))
        assertThat(priceMap["2"]).isEqualTo(BigDecimal("1.24").multiply(BigDecimal("2")))
    }
}
