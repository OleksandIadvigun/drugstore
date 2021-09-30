package sigma.software.leovegas.drugstore.store

import java.util.UUID
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import sigma.software.leovegas.drugstore.accountancy.client.proto.AccountancyClientProto
import sigma.software.leovegas.drugstore.api.messageSpliterator
import sigma.software.leovegas.drugstore.api.protobuf.Proto
import sigma.software.leovegas.drugstore.product.api.DeliverProductsQuantityRequest
import sigma.software.leovegas.drugstore.product.client.proto.ProductClientProto
import sigma.software.leovegas.drugstore.store.api.CheckStatusResponse
import sigma.software.leovegas.drugstore.store.api.TransferCertificateRequest
import sigma.software.leovegas.drugstore.store.api.TransferCertificateResponse
import sigma.software.leovegas.drugstore.store.api.TransferStatusDTO

@Service
@Transactional
class StoreService @Autowired constructor(
    val storeRepository: StoreRepository,
    val accountancyClientProto: AccountancyClientProto,
    val productClientProto: ProductClientProto,
) {

    val logger: Logger = LoggerFactory.getLogger(StoreService::class.java)

    fun createTransferCertificate(transferCertificateRequest: TransferCertificateRequest) =
        transferCertificateRequest.validate().run {
            val transferCertificate = storeRepository.save(
                TransferCertificate(
                    certificateNumber = UUID.randomUUID().toString(),
                    status = status.toEntity(),
                    orderNumber = orderNumber,
                    comment = comment
                )
            )
            logger.info("Transfer Certificate $transferCertificate")
            transferCertificate.toTransferCertificateResponse()
        }

    fun getTransferCertificatesByOrderNumber(orderNumber: String) = orderNumber.run {
        val transferCertificate = storeRepository.findAllByOrderNumber(orderNumber)
        logger.info("Transfer Certificate $transferCertificate")
        transferCertificate.toTransferCertificateResponseList()
    }


    fun getTransferCertificates(page: Int, size: Int): List<TransferCertificateResponse> {
        val pageable: Pageable = PageRequest.of(page, size)
        val transferCertificateList = storeRepository.findAll(pageable).content
        logger.info("Transfer Certificate list $transferCertificateList")
        return transferCertificateList.toTransferCertificateResponseList()
    }

    fun deliverProducts(orderNumber: String): TransferCertificateResponse =
        orderNumber.validate(storeRepository::getTransferCertificateByOrderNumber).run {

            val invoiceDetails: Proto.InvoiceDetails = runCatching {
                accountancyClientProto.getInvoiceDetailsByOrderNumber(this)
            }.onFailure { error ->
                throw AccountancyServerResponseException(error.localizedMessage.messageSpliterator())
            }
                .getOrThrow()
            logger.info("Received invoice details $invoiceDetails")

            val products = invoiceDetails.itemsList
                .map { DeliverProductsQuantityRequest(productNumber = it.productNumber, quantity = it.quantity) }

            checkAvailability(products)

            runCatching {
                productClientProto.deliverProducts(
                    Proto.DeliverProductsDTO.newBuilder().addAllItems(invoiceDetails.itemsList).build()
                )
            }
                .onFailure { error -> throw ProductServerResponseException(error.localizedMessage.messageSpliterator()) }
                .getOrThrow()
            logger.info("Products are delivered")

            val transferCertificate = createTransferCertificate(
                TransferCertificateRequest(this, TransferStatusDTO.DELIVERED, "products delivered")
            )
            logger.info("Transfer Certificate $transferCertificate")
            return@run transferCertificate
        }

    fun receiveProduct(orderNumber: String) =
        orderNumber.validate(storeRepository::getTransferCertificateByOrderNumber).run {

            val invoiceDetails: Proto.InvoiceDetails = runCatching {
                accountancyClientProto.getInvoiceDetailsByOrderNumber(this)
            }.onFailure { error ->
                throw AccountancyServerResponseException(error.localizedMessage.messageSpliterator())
            }
                .getOrThrow()
            logger.info("Received invoice details ${invoiceDetails.itemsList}")

            runCatching {
                productClientProto.receiveProducts(
                    Proto.ProductNumberList.newBuilder()
                        .addAllProductNumber(invoiceDetails.itemsList.map { it.productNumber }).build()
                )
            }
                .onFailure { error -> throw ProductServerResponseException(error.localizedMessage.messageSpliterator()) }
                .getOrThrow()
            logger.info("Products is received")

            val transferCertificate = createTransferCertificate(
                TransferCertificateRequest(this, TransferStatusDTO.RECEIVED, "products received")
            )
            logger.info("Transfer Certificate $transferCertificate")
            transferCertificate
        }

    fun checkAvailability(products: List<DeliverProductsQuantityRequest>) = products.validate().run {
        val productsMap = runCatching {
            productClientProto.getProductsDetailsByProductNumbers(products.map { it.productNumber }).productsList
                .associate { it.productNumber to it.quantity }
        }
            .onFailure { error -> throw ProductServerResponseException(error.localizedMessage.messageSpliterator()) }
            .getOrThrow()
        logger.info("Received product details ${productsMap.entries}")

        forEach {
            if (it.quantity > (productsMap[it.productNumber] ?: -1)) {
                throw InsufficientAmountOfProductException(it.productNumber, productsMap[it.productNumber] ?: -1)
            }
        }
        logger.info("Products quantity is sufficient $products")
        return@run products
    }

    fun checkTransfer(orderNumber: String) = orderNumber
        .validate(storeRepository::getTransferCertificateByOrderNumber)
        .run {
            logger.info("No transfer certificate was found")
            return@run CheckStatusResponse(this, "Not delivered")
        }
}
