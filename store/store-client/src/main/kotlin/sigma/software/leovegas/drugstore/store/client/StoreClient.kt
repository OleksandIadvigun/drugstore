package sigma.software.leovegas.drugstore.store.client

import feign.Headers
import feign.Param
import feign.RequestLine
import sigma.software.leovegas.drugstore.product.api.DeliverProductsQuantityRequest
import sigma.software.leovegas.drugstore.store.api.TransferCertificateResponse

@Headers("Content-Type: application/json")
interface StoreClient {

    @RequestLine("GET /api/v1/store/transfer-certificate")
    fun getTransferCertificates(): List<TransferCertificateResponse>

    @RequestLine("GET /api/v1/store/transfer-certificate/invoice/{id}")
    fun getTransferCertificatesByInvoiceId(@Param id: Long): List<TransferCertificateResponse>

    @RequestLine("PUT /api/v1/store/receive")
    fun receiveProducts(invoiceId: Long): TransferCertificateResponse

    @RequestLine("PUT /api/v1/store/deliver")
    fun deliverProducts(orderId: Long): TransferCertificateResponse

    @RequestLine("PUT /api/v1/store/availability")
    fun checkAvailability(products: List<DeliverProductsQuantityRequest>): List<DeliverProductsQuantityRequest>
}
