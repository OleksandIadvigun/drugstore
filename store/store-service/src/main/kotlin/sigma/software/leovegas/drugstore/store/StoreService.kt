package sigma.software.leovegas.drugstore.store

import org.slf4j.Logger
import org.slf4j.LoggerFactory
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

    val logger: Logger = LoggerFactory.getLogger(StoreService::class.java)

    fun createTransferCertificate(transferCertificateRequest: TransferCertificateRequest) =
        transferCertificateRequest.validate().run {
            val transferCertificate = storeRepository.save(this.toTransferCertificate())
            logger.info("Transfer Certificate $transferCertificate")
            transferCertificate.toTransferCertificateResponse()
        }

    fun getTransferCertificatesByOrderId(id: Long) = id.run {
        val transferCertificate = storeRepository.findAllByOrderId(id)
        logger.info("Transfer Certificate $transferCertificate")
        transferCertificate.toTransferCertificateResponseList()
    }


    fun getTransferCertificates(): List<TransferCertificateResponse> {
        val transferCertificateList = storeRepository.findAll()
        logger.info("Transfer Certificate list $transferCertificateList")
        return transferCertificateList.toTransferCertificateResponseList()
    }

    fun deliverProducts(orderId: Long): TransferCertificateResponse =
        orderId.validate(storeRepository::getTransferCertificateByOrderId).run {

            val invoiceDetails = runCatching { accountancyClient.getInvoiceDetailsByOrderId(this) }
                .onFailure { throw AccountancyServerResponseException(this) }
                .getOrNull()
                .orEmpty()
            logger.info("Received invoice details $invoiceDetails")
            val products = invoiceDetails
                .map { DeliverProductsQuantityRequest(id = it.productId, quantity = it.quantity) }

            checkAvailability(products)

            runCatching { productClient.deliverProducts(products) }
                .onFailure { throw ProductServerResponseException() }
                .getOrNull()
                .orEmpty()
            logger.info("Products are delivered")

            val transferCertificate = createTransferCertificate(
                TransferCertificateRequest(this, TransferStatusDTO.DELIVERED, "products delivered")
            )
            logger.info("Transfer Certificate $transferCertificate")
            return@run transferCertificate
        }

    fun receiveProduct(orderId: Long) =
        orderId.validate(storeRepository::getTransferCertificateByOrderId).run {

            val invoiceItems = runCatching { accountancyClient.getInvoiceDetailsByOrderId(this) }
                .onFailure { throw AccountancyServerResponseException(this) }
                .getOrNull()
                .orEmpty()
            logger.info("Received invoice details ${invoiceItems.toString()}")

            runCatching { productClient.receiveProducts(invoiceItems.map { it.productId }) }
                .onFailure { throw ProductServerResponseException() }
                .getOrNull()
                .orEmpty()
            logger.info("Products is received")

            val transferCertificate = createTransferCertificate(
                TransferCertificateRequest(this, TransferStatusDTO.RECEIVED, "products received")
            )
            logger.info("Transfer Certificate $transferCertificate")
            transferCertificate
        }

    fun checkAvailability(products: List<DeliverProductsQuantityRequest>) = products.validate().run {
        val productsMap = runCatching {
            productClient.getProductsDetailsByIds(products.map { it.id }).associate { it.id to it.quantity }
        }
            .onFailure { throw ProductServerResponseException() }
            .getOrThrow()
        logger.info("Received product details ${productsMap.entries}")

        forEach {
            if (it.quantity > (productsMap[it.id] ?: -1)) {
                throw InsufficientAmountOfProductException(it.id)
            }
        }
        logger.info("Products quantity is sufficient $products")
        return@run products
    }

    fun checkTransfer(orderNumber: Long): Long = orderNumber.validate(storeRepository::getTransferCertificateByOrderId)
}
