package sigma.software.leovegas.drugstore.accountancy.client.proto

import feign.Headers
import feign.Param
import feign.RequestLine
import org.springframework.cloud.openfeign.FeignClient
import sigma.software.leovegas.drugstore.api.protobuf.AccountancyProto

@Headers("Content-Type: application/x-protobuf")
@FeignClient(name = "FEIGN-PROTO", configuration = [AccountancyClientConfigurationProto::class])
interface AccountancyClientProto {

    @RequestLine("GET /api/v1/accountancy/invoice/details/order-number/{orderNumber}")
    fun getInvoiceDetailsByOrderNumber(@Param orderNumber: String): AccountancyProto.InvoiceDetails
}