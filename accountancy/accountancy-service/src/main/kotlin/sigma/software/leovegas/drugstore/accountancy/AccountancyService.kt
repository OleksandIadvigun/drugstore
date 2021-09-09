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
import sigma.software.leovegas.drugstore.accountancy.api.ItemDTO
import sigma.software.leovegas.drugstore.order.client.OrderClient
import sigma.software.leovegas.drugstore.product.api.CreateProductRequest
import sigma.software.leovegas.drugstore.product.client.ProductClient
import sigma.software.leovegas.drugstore.store.client.StoreClient

@Service
@Transactional
class AccountancyService @Autowired constructor(
    val orderClient: OrderClient,
    val invoiceRepository: InvoiceRepository,
    val storeClient: StoreClient,
    val productClient: ProductClient
) {

    val logger: Logger = LoggerFactory.getLogger(AccountancyService::class.java)

    fun createOutcomeInvoice(request: CreateOutcomeInvoiceRequest): ConfirmOrderResponse =
        request.validate(invoiceRepository::getInvoiceByOrderId).run {


            val productNumbers = productItems.map { it.productId }.distinct()
            val productQuantities = productItems.associate { it.productId to it.quantity }
            val productPrice = getSalePrice(productNumbers)

            val list = runCatching { productClient.getProductsDetailsByIds(productNumbers) }
                .onFailure { throw ProductServiceResponseException() }
                .getOrNull()
                .orEmpty()
            if (list.isEmpty()) throw OrderContainsInvalidProductsException(productNumbers)
            logger.info("Received products details $list")

            val invoiceItems = list.map {
                ProductItem(
                    productId = it.id,
                    name = it.name,
                    price = productPrice.getValue(it.id),
                    quantity = productQuantities.getValue(it.id),
                )
            }

            val invoice = invoiceRepository.save(
                Invoice(
                    type = InvoiceType.OUTCOME,
                    orderId = orderId,
                    status = InvoiceStatus.CREATED,
                    productItems = invoiceItems.toSet(),
                    total = invoiceItems.map { it.price.multiply(BigDecimal(it.quantity)) }.reduce(BigDecimal::plus)
                )
            )
            logger.info("Saved invoice $invoice")

            val confirmOrderResponse = ConfirmOrderResponse(
                orderId = invoice.orderId,
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
                .onFailure { throw ProductServiceResponseException() }
                .getOrNull()
                .orEmpty()
            logger.info("Created products $createdProducts")

            val invoice = invoiceRepository.save(Invoice(
                status = InvoiceStatus.CREATED,
                type = InvoiceType.INCOME,
                orderId = kotlin.random.Random(10000L).nextLong(),
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
                orderId = invoice.orderId,
                amount = invoice.total,
            )
            logger.info("Order confirmed $confirmOrderResponse")
            return@run confirmOrderResponse
        }

    fun getInvoiceById(id: Long): ConfirmOrderResponse =
        id.validate(invoiceRepository::findById).run {
            logger.info("Invoice found $this")
            this.toInvoiceResponse()
        }

    fun getInvoiceDetailsByOrderId(id: Long): List<ItemDTO> =
        id.validate { invoiceRepository.getInvoiceByOrderId(id) }.run {
            logger.info("Invoice found $this")
            if (status != InvoiceStatus.PAID) throw NotPaidInvoiceException(id)
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

    fun payInvoice(id: Long, money: BigDecimal): ConfirmOrderResponse =
        id.validate(invoiceRepository::findById).run {
            logger.info("Invoice found $this")

            if (this.status != InvoiceStatus.CREATED) throw InvalidStatusOfInvoice()
            if (money < this.total) throw NotEnoughMoneyException()

            val invoiceToSave = this.copy(status = InvoiceStatus.PAID)
            val invoice = invoiceRepository.saveAndFlush(invoiceToSave)
            logger.info("invoice paid $invoice")
            return@run invoice.toInvoiceResponse()
        }

    fun refundInvoice(id: Long): ConfirmOrderResponse =
        id.validate(invoiceRepository::findById).run {

            if (this.status != InvoiceStatus.PAID) throw NotPaidInvoiceException(this.id ?: -1)

            runCatching { storeClient.checkTransfer(this.orderId) }
                .onFailure { throw StoreServiceResponseException() }
            logger.info("Transfer was checked")

            val invoiceToSave = this.copy(status = InvoiceStatus.REFUND)
            val invoice = invoiceRepository.saveAndFlush(invoiceToSave)
            logger.info("invoice refund $invoice")
           return@run invoice.toInvoiceResponse()
        }

    fun cancelInvoice(id: Long): ConfirmOrderResponse =
        id.validate(invoiceRepository::findById).run {

            if (this.status.name == "PAID") throw InvoiceAlreadyPaidException(this.orderId)

            val invoiceToSave = this.copy(status = InvoiceStatus.CANCELLED)
            val invoice = invoiceRepository.saveAndFlush(invoiceToSave)
            logger.info("invoice cancelled $invoice")
            return@run invoice.toInvoiceResponse()
        }

    fun getSalePrice(ids: List<Long>): Map<Long, BigDecimal> =
        ids.validate().run {
            val profitTimes = BigDecimal(2.00)
            val prices = productClient.getProductPrice(this)
                .apply { this.values.map { it.multiply(profitTimes) } }
            logger.info("Sale prices $prices")
            prices
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

