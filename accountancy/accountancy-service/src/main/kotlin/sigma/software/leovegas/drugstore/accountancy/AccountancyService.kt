package sigma.software.leovegas.drugstore.accountancy

import java.math.BigDecimal
import java.time.LocalDateTime
import javax.transaction.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import sigma.software.leovegas.drugstore.accountancy.api.InvoiceRequest
import sigma.software.leovegas.drugstore.accountancy.api.InvoiceResponse
import sigma.software.leovegas.drugstore.accountancy.api.PriceItemRequest
import sigma.software.leovegas.drugstore.accountancy.api.PriceItemResponse
import sigma.software.leovegas.drugstore.accountancy.api.PurchasedCostsRequest
import sigma.software.leovegas.drugstore.accountancy.api.PurchasedCostsResponse
import sigma.software.leovegas.drugstore.order.api.OrderStatusDTO
import sigma.software.leovegas.drugstore.order.client.OrderClient
import sigma.software.leovegas.drugstore.store.api.CreateStoreRequest
import sigma.software.leovegas.drugstore.store.api.UpdateStoreRequest
import sigma.software.leovegas.drugstore.store.client.StoreClient

@Service
@Transactional
class AccountancyService @Autowired constructor(
    val purchasedCostsRepository: PurchasedCostsRepository,
    val priceItemRepository: PriceItemRepository,
    val orderClient: OrderClient,
    val invoiceRepository: InvoiceRepository,
    val storeClient: StoreClient
) {

    companion object {
        private const val exceptionMessage = "This price item with id: %d doesn't exist!"
        private const val messageForInvoice = "This product item with id: %d doesn't exist!"
    }

    fun createPriceItem(priceItemRequest: PriceItemRequest): PriceItemResponse = priceItemRequest.run {
        priceItemRepository.save(toEntity()).toPriceItemResponse()
    }

    fun updatePriceItem(id: Long, priceItemRequest: PriceItemRequest): PriceItemResponse = priceItemRequest.run {
        val toUpdate = priceItemRepository
            .findById(id)
            .orElseThrow { throw ResourceNotFoundException(String.format(exceptionMessage, id)) }
            .copy(productId = productId, price = price)
        priceItemRepository.saveAndFlush(toUpdate).toPriceItemResponse()
    }

    fun getProductsPrice(): Map<Long?, BigDecimal> =
        priceItemRepository.findAll().associate { it.productId to it.price }

    fun getProductsPriceByIds(ids: List<Long>): Map<Long?, BigDecimal> =
        priceItemRepository.findAllByProductId(ids).associate { it.productId to it.price }

    fun createPurchasedCosts(purchasedCostsRequest: PurchasedCostsRequest): PurchasedCostsResponse =
        purchasedCostsRequest.run {
            priceItemRepository.findById(this.priceItemId)
                .orElseThrow { throw PriceItemNotFoundException(this.priceItemId) }
            when {
                storeClient.getStoreItemsByPriceItemsId(listOf(this.priceItemId)).isEmpty() -> {
                    storeClient.createStoreItem(CreateStoreRequest(this.priceItemId, this.quantity))
                }
                else -> {
                    storeClient.increaseQuantity(listOf(UpdateStoreRequest(this.priceItemId, this.quantity)))
                }
            }
            purchasedCostsRepository.save(purchasedCostsRequest.toEntity()).toPurchasedCostsResponse()
        }

    fun getProductsPriceByProductIds(ids: List<Long>): List<PriceItemResponse> =
        priceItemRepository.findAllByProductId(ids).toPriceItemResponseList()

    fun getPriceItemsByIds(ids: List<Long>): List<PriceItemResponse> =
        priceItemRepository.findAllById(ids).toPriceItemResponseList()

    fun createInvoice(invoiceRequest: InvoiceRequest): InvoiceResponse = invoiceRequest.run {
        val isAlreadyExist = invoiceRepository.getInvoiceByOrderId(orderId)
        if (isAlreadyExist.isPresent) {
            throw OrderAlreadyHaveInvoice("This order already have some invoice")
        }
        val orderDetails = orderClient.getOrderDetails(orderId)
        val productItems = orderDetails.orderItemDetails.map {
            ProductItem(
                priceItemId = it.priceItemId,
                name = it.name,
                price = it.price,
                quantity = it.quantity,
            )
        }.toSet()
        val invoice = Invoice(
            orderId = orderId,
            total = orderDetails.total,
            productItems = productItems,
        )
        val toReduce = productItems.map { UpdateStoreRequest(it.priceItemId ?: -1, it.quantity) }
        storeClient.reduceQuantity(toReduce)
        orderClient.changeOrderStatus(orderId, OrderStatusDTO.BOOKED)
        invoiceRepository.save(invoice).toInvoiceResponse().copy(expiredAt = LocalDateTime.now().plusDays(3))
    }

    fun getInvoiceById(id: Long): InvoiceResponse = invoiceRepository
        .findById(id)
        .orElseThrow { throw ResourceNotFoundException(String.format(messageForInvoice, id)) }
        .toInvoiceResponse()

    fun getInvoiceByOrderId(id: Long): InvoiceResponse =
        invoiceRepository
            .getInvoiceByOrderId(id)
            .orElseThrow { throw ResourceNotFoundException(String.format(messageForInvoice, id)) }
            .toInvoiceResponse()

    fun payInvoice(id: Long): InvoiceResponse {
        val invoice = invoiceRepository.findById(id).orElseThrow {
            ResourceNotFoundException("Not found invoice with this id")
        }
        if (invoice.status != InvoiceStatus.CREATED) {
            throw InvalidStatusOfInvoice()
        }
        orderClient.changeOrderStatus(invoice.orderId ?: -1, OrderStatusDTO.PAID)
        val paidInvoice = invoice.copy(status = InvoiceStatus.PAID)
        return invoiceRepository.saveAndFlush(paidInvoice).toInvoiceResponse()
    }

    fun refundInvoice(id: Long): InvoiceResponse = id.run {
        val invoiceToRefund = invoiceRepository.findById(this).orElseThrow {
            ResourceNotFoundException("Not found invoice with this id")
        }
        if (invoiceToRefund.status != InvoiceStatus.PAID) {
            throw NotPaidInvoiceException(invoiceToRefund.id ?: -1)
        }
        orderClient.changeOrderStatus(invoiceToRefund.orderId ?: -1, OrderStatusDTO.REFUND)
        val refundInvoice = invoiceToRefund.copy(status = InvoiceStatus.REFUND)
        invoiceRepository.saveAndFlush(refundInvoice).toInvoiceResponse()
    }

    fun cancelInvoice(id: Long): InvoiceResponse {
        val invoice = invoiceRepository.findById(id).orElseThrow {
            ResourceNotFoundException("Not found invoice with this id")
        }
        if (invoice.status.name == "PAID") {
            throw OrderAlreadyHaveInvoice("This order is already paid. Please, first do refund!")
        }
        val toUpdate = invoice.copy(status = InvoiceStatus.CANCELLED)
        val toIncrease = toUpdate.productItems.map { UpdateStoreRequest(it.priceItemId ?: -1, it.quantity) }
        storeClient.increaseQuantity(toIncrease)
        orderClient.changeOrderStatus(invoice.orderId ?: -1, OrderStatusDTO.CANCELLED)
        return invoiceRepository.saveAndFlush(toUpdate).toInvoiceResponse()
    }

    fun cancelExpiredInvoice(date: LocalDateTime): List<InvoiceResponse> {
        val invoiceToCancelList =
            invoiceRepository.findAllByStatusAndCreatedAtLessThan(InvoiceStatus.CREATED, date)
        invoiceToCancelList.forEach {
            cancelInvoice(it.id ?: -1)
        }
        return invoiceToCancelList.toInvoiceResponseList()
    }
}
