package sigma.software.leovegas.drugstore.accountancy

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime
import javax.transaction.Transactional
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import sigma.software.leovegas.drugstore.accountancy.api.ConfirmOrderResponse
import sigma.software.leovegas.drugstore.accountancy.api.CreateIncomeInvoiceRequest
import sigma.software.leovegas.drugstore.accountancy.api.CreateOutcomeInvoiceRequest
import sigma.software.leovegas.drugstore.accountancy.api.InvoiceResponse
import sigma.software.leovegas.drugstore.accountancy.api.ItemDTO
import sigma.software.leovegas.drugstore.api.messageSpliterator
import sigma.software.leovegas.drugstore.product.api.CreateProductRequest
import sigma.software.leovegas.drugstore.product.client.ProductClient
import sigma.software.leovegas.drugstore.store.client.StoreClient

@Service
@Transactional
class AccountancyService @Autowired constructor(
    val invoiceRepository: InvoiceRepository,
    val storeClient: StoreClient,
    val productClient: ProductClient
) {

    val logger: Logger = LoggerFactory.getLogger(AccountancyService::class.java)

    fun createOutcomeInvoice(request: CreateOutcomeInvoiceRequest): ConfirmOrderResponse =
        request.validate(invoiceRepository::getInvoiceByOrderNumberAndStatusLike).run {

            val productNumbers = productItems.map { it.productId }.distinct()
            val productQuantities = productItems.associate { it.productId to it.quantity }
            val productPrice = getSalePrice(productNumbers)

            val list = runCatching { productClient.getProductsDetailsByIds(productNumbers) }
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
                    productId = it.productNumber,
                    name = it.name,
                    price = productPrice.getValue(it.productNumber),
                    quantity = productQuantities.getValue(it.productNumber),
                )
            }

            val invoice = invoiceRepository.save(
                Invoice(
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
                CreateProductRequest(
                    name = it.name,
                    price = it.price.setScale(2, RoundingMode.HALF_EVEN),
                    quantity = it.quantity
                )
            }
            val createdProducts = runCatching {
                productClient.createProduct(productsToCreate)
            }
                .onFailure { error -> throw ProductServiceResponseException(error.localizedMessage.messageSpliterator()) }
                .getOrNull()
                .orEmpty()
            logger.info("Created products $createdProducts")

            val invoice = invoiceRepository.save(Invoice(
                status = InvoiceStatus.PAID,
                type = InvoiceType.INCOME,
                orderNumber = (0..100000).random().toLong(),
                total = createdProducts.map { it.price.multiply(BigDecimal(it.quantity)) }
                    .reduce(BigDecimal::plus)
                    .setScale(2, RoundingMode.HALF_EVEN),
                productItems = createdProducts.map {
                    ProductItem(
                        productId = it.id,
                        name = it.name,
                        price = it.price,
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

    fun getInvoiceById(id: Long): InvoiceResponse =
        id.validate(invoiceRepository::findById).run {
            logger.info("Invoice found $this")
            this.toInvoiceResponseWithStatus()
        }

    fun getInvoiceDetailsByOrderNumber(orderNumber: Long): List<ItemDTO> =
        orderNumber.validate {
            invoiceRepository.getInvoiceByOrderNumberAndStatusNotLike(
                orderNumber,
                InvoiceStatus.CANCELLED
            )
        }
            .run {
                logger.info("Invoice found $this")
                if (status != InvoiceStatus.PAID) throw NotPaidInvoiceException(orderNumber)
                val productItems = productItems
                    .map {
                        ItemDTO(
                            productId = it.productId,
                            quantity = it.quantity
                        )
                    }
                logger.info("Invoice details $productItems")
                return@run productItems
            }

    fun payInvoice(orderNumber: Long, money: BigDecimal): ConfirmOrderResponse =
        orderNumber.validate(invoiceRepository::getInvoiceByOrderNumber).run {
            logger.info("Invoice found $this")

            if (this.status != InvoiceStatus.CREATED) throw InvalidStatusOfInvoice()
            if (money < this.total) throw NotEnoughMoneyException()

            val invoiceToSave = this.copy(status = InvoiceStatus.PAID)
            val invoice = invoiceRepository.saveAndFlush(invoiceToSave)
            logger.info("invoice paid $invoice")
            return@run invoice.toInvoiceResponse()
        }

    fun refundInvoice(orderNumber: Long): ConfirmOrderResponse =
        orderNumber.validate(invoiceRepository::getInvoiceByOrderNumber).run {

            if (this.status != InvoiceStatus.PAID) throw NotPaidInvoiceException(this.id ?: -1)

            runCatching { storeClient.checkTransfer(this.orderNumber) }
                .onFailure { error -> throw StoreServiceResponseException(error.localizedMessage.messageSpliterator()) }
            logger.info("Transfer was checked")

            val invoiceToSave = this.copy(status = InvoiceStatus.REFUND)
            val invoice = invoiceRepository.saveAndFlush(invoiceToSave)
            logger.info("invoice refund $invoice")
            return@run invoice.toInvoiceResponse()
        }

    fun cancelInvoice(orderNumber: Long): ConfirmOrderResponse =
        orderNumber.validate(invoiceRepository::getInvoiceByOrderNumber).run {

            if (this.status.name == "PAID") throw InvoiceAlreadyPaidException(this.orderNumber)

            val invoiceToSave = this.copy(status = InvoiceStatus.CANCELLED)
            val invoice = invoiceRepository.saveAndFlush(invoiceToSave)
            logger.info("invoice cancelled $invoice")
            return@run invoice.toInvoiceResponse()
        }

    fun getSalePrice(ids: List<Long>): Map<Long, BigDecimal> =
        ids.validate().run {
            val profitTimes = BigDecimal(2.00)
            val prices = runCatching {
                productClient.getProductPrice(this)
                    .mapValues { it.value.multiply(profitTimes) }
            }
                .onFailure { error -> throw ProductServiceResponseException(error.localizedMessage.messageSpliterator()) }
                .getOrThrow()
            logger.info("Received prices $prices")
            return@run prices
        }

    fun cancelExpiredInvoice(date: LocalDateTime): List<ConfirmOrderResponse> {
        val invoiceToCancelList = invoiceRepository.findAllByStatusAndCreatedAtLessThan(InvoiceStatus.CREATED, date)
        invoiceToCancelList.forEach {
            cancelInvoice(it.id ?: -1)
            logger.info("Invoice $it.id status was changed to cancel")
        }
        return invoiceToCancelList.toInvoiceResponseList()
    }
}
