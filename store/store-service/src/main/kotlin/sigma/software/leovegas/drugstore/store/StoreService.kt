package sigma.software.leovegas.drugstore.store

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import sigma.software.leovegas.drugstore.accountancy.client.AccountancyClient
import sigma.software.leovegas.drugstore.api.messageSpliterator
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
        val transferCertificate = storeRepository.findAllByOrderNumber(id)
        logger.info("Transfer Certificate $transferCertificate")
        transferCertificate.toTransferCertificateResponseList()
    }


    fun getTransferCertificates(): List<TransferCertificateResponse> {
        val transferCertificateList = storeRepository.findAll()
        logger.info("Transfer Certificate list $transferCertificateList")
        return transferCertificateList.toTransferCertificateResponseList()
    }

    fun deliverProducts(orderNumber: Long): TransferCertificateResponse =
        orderNumber.validate(storeRepository::getTransferCertificateByOrderNumber).run {

            val invoiceDetails = runCatching {
                accountancyClient.getInvoiceDetailsByOrderNumber(this)
            }
                .onFailure { error ->
                    throw AccountancyServerResponseException(error.localizedMessage.messageSpliterator())
                }
                .getOrNull()
                .orEmpty()
            logger.info("Received invoice details $invoiceDetails")
            val products = invoiceDetails
                .map { DeliverProductsQuantityRequest(id = it.productId, quantity = it.quantity) }

            checkAvailability(products)
            runCatching { productClient.deliverProducts(products) }
                .onFailure { error -> throw ProductServerResponseException(error.localizedMessage.messageSpliterator()) }
                .getOrNull()
                .orEmpty()
            logger.info("Products are delivered")

            val transferCertificate = createTransferCertificate(
                TransferCertificateRequest(this, TransferStatusDTO.DELIVERED, "products delivered")
            )
            logger.info("Transfer Certificate $transferCertificate")
            return@run transferCertificate
        }

    fun receiveProduct(orderNumber: Long) =
        orderNumber.validate(storeRepository::getTransferCertificateByOrderNumber).run {

            val invoiceItems = runCatching { accountancyClient.getInvoiceDetailsByOrderNumber(this) }
                .onFailure { exception ->
                    throw AccountancyServerResponseException(exception.localizedMessage.messageSpliterator())
                }
                .getOrNull()
                .orEmpty()
            logger.info("Received invoice details ${invoiceItems.toString()}")

            runCatching { productClient.receiveProducts(invoiceItems.map { it.productId }) }
                .onFailure { error -> throw ProductServerResponseException(error.localizedMessage.messageSpliterator()) }
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
            productClient.getProductsDetailsByIds(products.map { it.id }).associate { it.productNumber to it.quantity }
        }
            .onFailure { error -> throw ProductServerResponseException(error.localizedMessage.messageSpliterator()) }
            .getOrThrow()
        logger.info("Received product details ${productsMap.entries}")

        forEach {
            if (it.quantity > (productsMap[it.id] ?: -1)) {
                throw InsufficientAmountOfProductException(it.id, productsMap[it.id] ?: -1)
            }
        }
        logger.info("Products quantity is sufficient $products")
        return@run products
    }

    fun checkTransfer(orderNumber: Long): Long =
        orderNumber.validate(storeRepository::getTransferCertificateByOrderNumber)
}
