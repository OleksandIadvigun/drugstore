package sigma.software.leovegas.drugstore.store

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
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

    fun getTransferCertificatesByOrderId(id: Long) =
        storeRepository.findAllByOrderId(id).toTransferCertificateResponseList()

    fun getTransferCertificates() = storeRepository.findAll().toTransferCertificateResponseList()

    fun deliverProducts(orderId: Long): TransferCertificateResponse =
        orderId.validate(storeRepository::getTransferCertificateByOrderId).run {

            val invoiceDetails = runCatching { accountancyClient.getInvoiceDetailsByOrderId(this) }
                .onFailure { throw AccountancyServerResponseException(this) }
                .getOrNull()
                .orEmpty()

            val products = invoiceDetails
                .map { DeliverProductsQuantityRequest(id = it.productId, quantity = it.quantity) }

            checkAvailability(products)

            runCatching { productClient.deliverProducts(products) }
                .onFailure { throw ProductServerResponseException(this) }
                .getOrNull()
                .orEmpty()

            return@run createTransferCertificate(
                TransferCertificateRequest(
                    this,
                    TransferStatusDTO.DELIVERED,
                    "products delivered"
                )
            )
        }

    fun receiveProduct(orderId: Long) =
        orderId.validate(storeRepository::getTransferCertificateByOrderId).run {

            val invoiceItems = runCatching { accountancyClient.getInvoiceDetailsByOrderId(this) }
                .onFailure { throw AccountancyServerResponseException(this) }
                .getOrNull()
                .orEmpty()

            runCatching { productClient.receiveProducts(invoiceItems.map { it.productId }) }
                .onFailure { throw ProductServerResponseException(this) }
                .getOrNull()
                .orEmpty()

            createTransferCertificate(
                TransferCertificateRequest(this, TransferStatusDTO.RECEIVED, "products received")
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
