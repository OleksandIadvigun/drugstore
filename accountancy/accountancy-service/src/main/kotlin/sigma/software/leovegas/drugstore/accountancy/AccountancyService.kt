package sigma.software.leovegas.drugstore.accountancy

import java.math.BigDecimal
import java.math.RoundingMode
import java.util.UUID
import javax.transaction.Transactional
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.stream.function.StreamBridge
import org.springframework.stereotype.Service
import sigma.software.leovegas.drugstore.accountancy.api.ConfirmOrderResponse
import sigma.software.leovegas.drugstore.accountancy.api.CreateIncomeInvoiceRequest
import sigma.software.leovegas.drugstore.accountancy.api.CreateOutcomeInvoiceEvent
import sigma.software.leovegas.drugstore.accountancy.api.InvoiceResponse
import sigma.software.leovegas.drugstore.api.messageSpliterator
import sigma.software.leovegas.drugstore.api.protobuf.Proto
import sigma.software.leovegas.drugstore.api.toBigDecimal
import sigma.software.leovegas.drugstore.api.toDecimalProto
import sigma.software.leovegas.drugstore.product.api.CreateProductRequest
import sigma.software.leovegas.drugstore.product.api.CreateProductsEvent
import sigma.software.leovegas.drugstore.product.client.proto.ProductClientProto
import sigma.software.leovegas.drugstore.store.client.proto.StoreClientProto

@Service
@Transactional
class AccountancyService @Autowired constructor(
    val invoiceRepository: InvoiceRepository,
    val storeClientProto: StoreClientProto,
    val productClientProto: ProductClientProto,
    val eventStream: StreamBridge,
) {

    val logger: Logger = LoggerFactory.getLogger(AccountancyService::class.java)

    fun createOutcomeInvoice(event: Proto.CreateOutcomeInvoiceEvent): ConfirmOrderResponse =
        event.validate(invoiceRepository::getInvoiceByOrderNumberAndStatusLike).run {

            val productNumbers = productItemsList.map { it.productNumber }.distinct()
            val productQuantities = productItemsList.associate { it.productNumber to it.quantity }
            val productPrice = getSalePrice(productNumbers)

            val list =
                runCatching { productClientProto.getProductsDetailsByProductNumbers(productNumbers).productsList }
                    .onFailure { error -> throw ProductServiceResponseException(error.localizedMessage.messageSpliterator()) }
                    .getOrNull()
                    .orEmpty()
            if (list.isEmpty()) throw OrderContainsInvalidProductsException(productNumbers)
            logger.info("Received products details $list")

            val invoiceItems = list.map {
                if (it.quantity < productQuantities.getValue(it.productNumber))              // todo
                    logger.error(
                        " Not enough quantity of product ${it.productNumber}, you should buy: " +
                                "${productQuantities.getValue(it.productNumber) - it.quantity}" +
                                " item(s) from dealer for realization of order $orderNumber"
                    )

                ProductItem(
                    productNumber = it.productNumber,
                    name = it.name,
                    price = productPrice.itemsMap.getValue(it.productNumber).toBigDecimal(),
                    quantity = productQuantities.getValue(it.productNumber),
                )
            }

            val invoice = invoiceRepository.save(
                Invoice(
                    invoiceNumber = UUID.randomUUID().toString(),
                    type = InvoiceType.OUTCOME,
                    orderNumber = orderNumber,
                    status = InvoiceStatus.CREATED,
                    productItems = invoiceItems.toSet(),
                    total = invoiceItems.map { it.price.multiply(BigDecimal(it.quantity)) }.reduce(BigDecimal::plus)
                )
            )
            logger.info("Saved invoice $invoice")

            val confirmOrderResponse = ConfirmOrderResponse(
                orderNumber = invoice.orderNumber,
                amount = invoice.total
            )
            logger.info("Order confirmed $confirmOrderResponse")
            confirmOrderResponse
        }

    fun createIncomeInvoice(invoiceRequest: CreateIncomeInvoiceRequest): ConfirmOrderResponse =
        invoiceRequest.validate().run {

            val productsToCreate = productItems.map {
                Proto.ProductDetailsItem.newBuilder()
                    .setProductNumber(UUID.randomUUID().toString())
                    .setName(it.name)
                    .setQuantity(it.quantity)
                    .setPrice(it.price.setScale(2, RoundingMode.HALF_EVEN).toDecimalProto())
                    .build()
            }

            val createProtoEvent = Proto.CreateProductsEvent.newBuilder().addAllProducts(productsToCreate).build()
                runCatching { eventStream.send("createProductEventPublisher-out-0", createProtoEvent) }
                    .onFailure { error -> throw ProductServiceResponseException(error.localizedMessage.messageSpliterator()) }
                    .getOrThrow()

            val invoice = invoiceRepository.save(
                Invoice(
                    invoiceNumber = UUID.randomUUID().toString(),
                    status = InvoiceStatus.PAID,
                    type = InvoiceType.INCOME,
                    orderNumber = UUID.randomUUID().toString(),
                    total = productsToCreate.map { it.price.toBigDecimal().multiply(BigDecimal(it.quantity)) }
                        .reduce(BigDecimal::plus)
                        .setScale(2, RoundingMode.HALF_EVEN),
                    productItems = productsToCreate.map {
                        ProductItem(
                            productNumber = it.productNumber,
                            name = it.name,
                            price = it.price.toBigDecimal(),
                            quantity = it.quantity,
                        )
                    }.toSet(),
                )
            )

            logger.info("Saved invoice $invoice")
            val confirmOrderResponse = ConfirmOrderResponse(
                orderNumber = invoice.orderNumber,
                amount = invoice.total,
            )
            logger.info("Order confirmed $confirmOrderResponse")
            return@run confirmOrderResponse
        }

    fun getInvoiceByInvoiceNumber(invoiceNumber: String): InvoiceResponse =
        invoiceNumber.validate(invoiceRepository::getInvoiceByInvoiceNumber).run {
            logger.info("Invoice found $this")
            this.toInvoiceResponseWithStatus()
        }

    fun getInvoiceDetailsByOrderNumber(orderNumber: String): Proto.InvoiceDetails =
        orderNumber.validate {
            invoiceRepository.getInvoiceByOrderNumberAndStatusNotLike(
                orderNumber,
                InvoiceStatus.CANCELLED
            )
        }
            .run {
                logger.info("Invoice found $this")
                if (status != InvoiceStatus.PAID) throw NotPaidInvoiceException(orderNumber)
                val invoiceItems = productItems.map {
                    Proto.Item.newBuilder()
                        .setProductNumber(it.productNumber)
                        .setQuantity(it.quantity)
                        .build()
                }
                logger.info("Invoice details $productItems")
                return@run Proto.InvoiceDetails.newBuilder().addAllItems(invoiceItems).build()
            }

    fun payInvoice(orderNumber: String, money: BigDecimal): ConfirmOrderResponse =
        orderNumber.validate(invoiceRepository::getInvoiceByOrderNumber).run {
            logger.info("Invoice found $this")

            if (this.status != InvoiceStatus.CREATED) throw InvalidStatusOfInvoice()
            if (money < this.total) throw NotEnoughMoneyException()

            val invoiceToSave = this.copy(status = InvoiceStatus.PAID)
            val invoice = invoiceRepository.saveAndFlush(invoiceToSave)
            logger.info("invoice paid $invoice")
            return@run invoice.toConfirmOrderResponse()
        }

    fun refundInvoice(orderNumber: String): ConfirmOrderResponse =
        orderNumber.validate(invoiceRepository::getInvoiceByOrderNumber).run {

            if (this.status != InvoiceStatus.PAID) throw NotPaidInvoiceException(this.invoiceNumber)

            runCatching {
                storeClientProto.checkTransfer(this.orderNumber)
                logger.info("checked")
            }
                .onFailure { error -> throw StoreServiceResponseException(error.localizedMessage.messageSpliterator()) }
            logger.info("Transfer was checked")

            val invoiceToSave = this.copy(status = InvoiceStatus.REFUND)
            val invoice = invoiceRepository.saveAndFlush(invoiceToSave)
            logger.info("invoice refund $invoice")
            return@run invoice.toConfirmOrderResponse()
        }

    fun cancelInvoice(orderNumber: String): ConfirmOrderResponse =
        orderNumber.validate(invoiceRepository::getInvoiceByOrderNumber).run {

            if (this.status.name == "PAID") throw InvoiceAlreadyPaidException(this.orderNumber)

            val invoiceToSave = this.copy(status = InvoiceStatus.CANCELLED)
            val invoice = invoiceRepository.saveAndFlush(invoiceToSave)
            logger.info("invoice cancelled $invoice")
            return@run invoice.toConfirmOrderResponse()
        }

    fun getSalePrice(productNumbers: List<String>): Proto.ProductsPrice =
        productNumbers.validate().run {
            val profitTimes = BigDecimal(2.00)
            val prices = runCatching {
                productClientProto.getProductPrice(this).itemsMap
                    .mapValues { it.value.toBigDecimal().multiply(profitTimes).toDecimalProto() }
            }
                .onFailure { error -> throw ProductServiceResponseException(error.localizedMessage.messageSpliterator()) }
                .getOrThrow()
            logger.info("Received prices $prices")
            return@run Proto.ProductsPrice.newBuilder().putAllItems(prices).build()
        }
}
