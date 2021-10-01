package sigma.software.leovegas.drugstore.store.client

import feign.Headers
import feign.Param
import feign.RequestLine
import sigma.software.leovegas.drugstore.product.api.DeliverProductsQuantityRequest
import sigma.software.leovegas.drugstore.store.api.TransferCertificateResponse

@Headers("Content-Type: application/json")
interface StoreClient {

    @RequestLine("GET /api/v1/store/transfer-certificate?page={page}&size={size}")
    fun getTransferCertificates(
        @Param("page") page: Int = 0,
        @Param("size") size: Int = 5,
    ): List<TransferCertificateResponse>

    @RequestLine("GET /api/v1/store/transfer-certificate/order/{orderNumber}")
    fun getTransferCertificatesByOrderNumber(@Param orderNumber: String): TransferCertificateResponse

    @RequestLine("PUT /api/v1/store/receive/{orderNumber}")
    fun receiveProducts(@Param orderNumber: String): TransferCertificateResponse

    @RequestLine("PUT /api/v1/store/deliver/{orderNumber}")
    fun deliverProducts(@Param orderNumber: String): TransferCertificateResponse

    @RequestLine("PUT /api/v1/store/availability")
    fun checkAvailability(products: List<DeliverProductsQuantityRequest>): List<DeliverProductsQuantityRequest>
}
