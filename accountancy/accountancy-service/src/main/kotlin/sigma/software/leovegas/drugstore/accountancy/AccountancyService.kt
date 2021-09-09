package sigma.software.leovegas.drugstore.accountancy

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime
import javax.transaction.Transactional
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

    fun createOutcomeInvoice(request: CreateOutcomeInvoiceRequest): ConfirmOrderResponse =
        request.validate(invoiceRepository::getInvoiceByOrderId).run {

            val profitTimes = BigDecimal(2.00)
            val productQuantities = productItems.associate { it.productId to it.quantity }
            val total = productItems
                .map { it.productId to it.quantity }
                .map { productClient.getProductPrice(it.first) to it.second }
                .map { it.first.multiply(BigDecimal(it.second)).multiply(profitTimes) }
                .reduce(BigDecimal::add)

            val productNumbers = productItems.map { it.productId }.distinct()
            val list = runCatching { productClient.getProductsDetailsByIds(productNumbers) }
                .onFailure { throw ProductServiceResponseException() }
                .getOrNull()
                .orEmpty()
            if (list.isEmpty()) throw OrderContainsInvalidProductsException(productNumbers)

            val invoice = invoiceRepository.save(
                Invoice(
                    type = InvoiceType.OUTCOME,
                    orderId = orderId,
                    total = total,
                    status = InvoiceStatus.CREATED,
                    productItems = list
                        .map {
                            ProductItem(
                                productId = it.id,
                                name = it.name,
                                price = it.price.multiply(profitTimes).setScale(2, RoundingMode.HALF_EVEN),
                                quantity = productQuantities.getValue(it.id),
                            )
                        }
                        .toSet(),
                )
            )

            ConfirmOrderResponse(
                orderId = invoice.orderId,
                amount = invoice.total
            )
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

            val invoice = invoiceRepository.save(Invoice(
                status = InvoiceStatus.CREATED,
                type = InvoiceType.INCOME,
                orderId = kotlin.random.Random(10000L).nextLong(),   // todo
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
            return@run ConfirmOrderResponse(
                orderId = invoice.orderId,
                amount = invoice.total,
            )
        }

    fun getInvoiceById(id: Long): ConfirmOrderResponse =
        id.validate(invoiceRepository::findById).toInvoiceResponse()

    fun getInvoiceDetailsByOrderId(id: Long): List<ItemDTO> =
        id.validate { invoiceRepository.getInvoiceByOrderId(id) }.run {
            if (status != InvoiceStatus.PAID) throw NotPaidInvoiceException(id)
            productItems
                .map {
                    ItemDTO(
                        productId = it.productId,
                        quantity = it.quantity
                    )
                }
        }

    fun payInvoice(id: Long, money: BigDecimal): ConfirmOrderResponse =
        id.validate(invoiceRepository::findById).run {
            if (this.status != InvoiceStatus.CREATED) throw InvalidStatusOfInvoice()
            if (money < this.total) throw NotEnoughMoneyException()
            val paidInvoice = this.copy(status = InvoiceStatus.PAID)
            return invoiceRepository.saveAndFlush(paidInvoice).toInvoiceResponse()
        }

    fun refundInvoice(id: Long): ConfirmOrderResponse =
        id.validate(invoiceRepository::findById).run {
            if (this.status != InvoiceStatus.PAID) throw NotPaidInvoiceException(this.id ?: -1)
            runCatching { storeClient.checkTransfer(this.orderId) }
                .onFailure { throw StoreServiceResponseException() }
            val refundInvoice = this.copy(status = InvoiceStatus.REFUND)
            invoiceRepository.saveAndFlush(refundInvoice).toInvoiceResponse()
        }

    fun cancelInvoice(id: Long): ConfirmOrderResponse =
        id.validate(invoiceRepository::findById).run {
            if (this.status.name == "PAID") throw InvoiceAlreadyPaidException(this.orderId)
            val toUpdate = this.copy(status = InvoiceStatus.CANCELLED)
            return invoiceRepository.saveAndFlush(toUpdate).toInvoiceResponse()
        }

    fun cancelExpiredInvoice(date: LocalDateTime): List<ConfirmOrderResponse> {
        val invoiceToCancelList = invoiceRepository.findAllByStatusAndCreatedAtLessThan(InvoiceStatus.CREATED, date)
        invoiceToCancelList.forEach { cancelInvoice(it.id ?: -1) }
        return invoiceToCancelList.toInvoiceResponseList()
    }
}
