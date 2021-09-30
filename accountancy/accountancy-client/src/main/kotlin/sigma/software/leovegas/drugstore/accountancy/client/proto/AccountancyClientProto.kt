package sigma.software.leovegas.drugstore.accountancy.client.proto

import feign.Headers
import feign.Param
import feign.RequestLine
import sigma.software.leovegas.drugstore.api.protobuf.Proto

@Headers("Content-Type: application/x-protobuf")
interface AccountancyClientProto {

    @RequestLine("GET /api/v1/accountancy/invoice/details/order-number/{orderNumber}")
    fun getInvoiceDetailsByOrderNumber(@Param orderNumber: String): Proto.InvoiceDetails
}
