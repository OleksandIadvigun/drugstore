package sigma.software.leovegas.drugstore.store

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import sigma.software.leovegas.drugstore.accountancy.api.InvoiceStatusDTO
import sigma.software.leovegas.drugstore.accountancy.api.InvoiceTypeDTO
import sigma.software.leovegas.drugstore.accountancy.client.AccountancyClient
import sigma.software.leovegas.drugstore.product.api.DeliverProductsQuantityRequest
import sigma.software.leovegas.drugstore.product.client.ProductClient
import sigma.software.leovegas.drugstore.store.api.TransferCertificateRequest
import sigma.software.leovegas.drugstore.store.api.TransferCertificateResponse
import sigma.software.leovegas.drugstore.store.api.TransferStatusDTO

@Service
@Transactional
class StoreService @Autowired constructor(
    val storeRepository: StoreRepository,
    val accountancyClient: AccountancyClient,
    val productClient: ProductClient,
) {

    fun createTransferCertificate(transferCertificateRequest: TransferCertificateRequest) =
        storeRepository.save(transferCertificateRequest.toTransferCertificate()).toTransferCertificateResponse()

    fun getTransferCertificatesByInvoiceId(id: Long) =
        storeRepository.findAllByInvoiceId(id).toTransferCertificateResponseList()

    fun getTransferCertificates() = storeRepository.findAll().toTransferCertificateResponseList()

    fun deliverProducts(orderId: Long): TransferCertificateResponse {
        val invoice = accountancyClient.getInvoiceByOrderId(orderId)
        if (invoice.type != InvoiceTypeDTO.OUTCOME) {
            throw IncorrectTypeOfInvoice("Invoice type should be outcome")
        }
        when (invoice.status) {
            InvoiceStatusDTO.CREATED -> throw IncorrectStatusOfInvoice("Invoice is not paid")
            InvoiceStatusDTO.CANCELLED -> throw IncorrectStatusOfInvoice("Invoice is already cancelled")
            InvoiceStatusDTO.REFUND -> throw IncorrectStatusOfInvoice("You are already waiting refund")
        }
        val products =
            invoice.productItems.map { DeliverProductsQuantityRequest(id = it.productId, quantity = it.quantity) }
        checkAvailability(products)
        productClient.deliverProducts(products)
        return createTransferCertificate(
            TransferCertificateRequest(
                invoice.id,
                TransferStatusDTO.DELIVERED,
                "products delivered"
            )
        )
    }

    fun receiveProduct(invoiceId: Long) = invoiceId.run {
        val invoice = accountancyClient.getInvoiceById(invoiceId)
        if (invoice.type != InvoiceTypeDTO.INCOME) throw IncorrectTypeOfInvoice("Invoice type must be income")
        when (invoice.status) {
            InvoiceStatusDTO.CANCELLED -> throw IncorrectStatusOfInvoice("Invoice is cancelled")
            InvoiceStatusDTO.CREATED -> throw IncorrectStatusOfInvoice("Invoice is not paid")
            InvoiceStatusDTO.REFUND -> throw IncorrectStatusOfInvoice("Refund is done")
        }
        productClient.receiveProducts(invoice.productItems.map { it.productId })
        createTransferCertificate(
            TransferCertificateRequest(invoiceId, TransferStatusDTO.RECEIVED, "products received")
        )
    }

    fun checkAvailability(products: List<DeliverProductsQuantityRequest>) = products.run {
        val productsMap =
            productClient.getProductsDetailsByIds(products.map { it.id }).associate { it.id to it.quantity }
        forEach {
            if (it.quantity > (productsMap[it.id] ?: -1)) {
                throw InsufficientAmountOfProductException(it.id)
            }
        }
        products
    }
}
