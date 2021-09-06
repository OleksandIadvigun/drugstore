package sigma.software.leovegas.drugstore.accountancy

import java.math.BigDecimal
import java.time.LocalDateTime
import javax.transaction.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import sigma.software.leovegas.drugstore.accountancy.api.CreateIncomeInvoiceRequest
import sigma.software.leovegas.drugstore.accountancy.api.CreateOutcomeInvoiceRequest
import sigma.software.leovegas.drugstore.accountancy.api.InvoiceResponse
import sigma.software.leovegas.drugstore.order.api.OrderStatusDTO
import sigma.software.leovegas.drugstore.order.client.OrderClient
import sigma.software.leovegas.drugstore.product.api.CreateProductRequest
import sigma.software.leovegas.drugstore.product.api.CreateProductResponse
import sigma.software.leovegas.drugstore.product.api.ProductDetailsResponse
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

    companion object {
        private const val exceptionMessage = "This price item with id: %d doesn't exist!"
        private const val messageForNotFoundInvoice = "The invoice with id: %d doesn't exist!"
    }

    fun createOutcomeInvoice(request: CreateOutcomeInvoiceRequest): InvoiceResponse = request.run {
        val isAlreadyExist = invoiceRepository.getInvoiceByOrderId(orderId)
        if (isAlreadyExist.isPresent) {
            throw OrderAlreadyHaveInvoice("This order already has some invoice")
        }
        val productIdToQuantity = productItems.associate { it.productId to it.quantity }
        val products: List<ProductDetailsResponse>
        try {
            products = productClient.getProductsDetailsByIds(productIdToQuantity.keys.toList())
        } catch (e: Exception) {
            throw ProductServiceResponseException()
        }
        val productItems = products.map {
            ProductItem(
                productId = it.id,
                name = it.name,
                price = it.price,
                quantity = it.quantity,
            )
        }.toSet()
        val invoice = Invoice(
            type = InvoiceType.OUTCOME,
            orderId = orderId,
            total = products.map { it.price * ((productIdToQuantity[it.id]?.toBigDecimal() ?: BigDecimal.ONE)) }
                .reduce { acc, bigDecimal -> acc + bigDecimal },
            productItems = productItems,
        )
        invoiceRepository.save(invoice).toInvoiceResponse().copy(expiredAt = LocalDateTime.now().plusDays(3)) //todo
    }

    fun createIncomeInvoice(invoiceRequest: CreateIncomeInvoiceRequest): InvoiceResponse = invoiceRequest.run {
        val productsToCreate = productItems.map {
            CreateProductRequest(
                name = it.name,
                price = it.price,
                quantity = it.quantity
            )
        }
        val createdProducts: List<CreateProductResponse>
        try {
            createdProducts = productClient.createProduct(productsToCreate)
        } catch (e: Exception) {
            throw ProductServiceResponseException()
        }
        val productItems = createdProducts.map {
            ProductItem(
                productId = it.id,
                name = it.name,
                price = it.price,
                quantity = it.quantity,
            )
        }.toSet()
        val invoice = Invoice(
            type = InvoiceType.INCOME,
            total = createdProducts.map { it.price * it.quantity.toBigDecimal() }
                .reduce { acc, bigDecimal -> acc + bigDecimal },
            productItems = productItems,
        )
        invoiceRepository.save(invoice).toInvoiceResponse().copy(expiredAt = LocalDateTime.now().plusDays(3)) //todo
    }

    fun getInvoiceById(id: Long): InvoiceResponse = invoiceRepository
        .findById(id)
        .orElseThrow { throw ResourceNotFoundException(String.format(messageForNotFoundInvoice, id)) }
        .toInvoiceResponse()

    fun getInvoiceByOrderId(id: Long): InvoiceResponse =
        invoiceRepository
            .getInvoiceByOrderId(id)
            .orElseThrow { throw ResourceNotFoundException(String.format(messageForNotFoundInvoice, id)) }
            .toInvoiceResponse()

    fun payInvoice(id: Long, money: BigDecimal): InvoiceResponse {
        val invoice = invoiceRepository.findById(id).orElseThrow {
            ResourceNotFoundException("Not found invoice with this id")
        }
        if (invoice.status != InvoiceStatus.CREATED) {
            throw InvalidStatusOfInvoice()
        }
        if (money < invoice.total) {
            throw NotEnoughMoneyException()
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
        try {
            orderClient.changeOrderStatus(invoiceToRefund.orderId ?: -1, OrderStatusDTO.REFUND)
        } catch (e: Exception) {
            throw OrderServiceResponseException()
        }
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
        try {
            orderClient.changeOrderStatus(invoice.orderId ?: -1, OrderStatusDTO.CANCELLED)
        } catch (e: Exception) {
            throw OrderServiceResponseException()
        }
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

//    private fun markupChecker(priceItems: List<PriceItem>, markup: Boolean): List<PriceItemResponse> {
//        return if (markup) {
//            priceItems.map { el ->
//                val newPrice = (el.price * (BigDecimal(1.00) + el.markup)).setScale(2)
//                el.copy(price = newPrice)
//            }
//                .toPriceItemResponseList()
//        } else priceItems.toPriceItemResponseList()
//    }
//
//    fun getMarkUps(ids: List<Long>): List<MarkupUpdateResponse> =
//        if (ids.isNotEmpty()) {
//            priceItemRepository.findAllById(ids).toMarkupUpdateResponse()
//        } else priceItemRepository.findAll().toMarkupUpdateResponse()
//
//    fun updateMarkups(markupsToUpdate: List<MarkupUpdateRequest>): List<MarkupUpdateResponse> = markupsToUpdate.run {
//        val priceItemToMarkup = associate { it.priceItemId to it.markup.setScale(2, RoundingMode.DOWN) }
//        val toUpdate = priceItemRepository
//            .findAllById(priceItemToMarkup.keys)
//            .map { it.copy(markup = priceItemToMarkup[it.id] ?: BigDecimal.ZERO) }
//        priceItemRepository.saveAllAndFlush(toUpdate).toMarkupUpdateResponse()
//    }
}
