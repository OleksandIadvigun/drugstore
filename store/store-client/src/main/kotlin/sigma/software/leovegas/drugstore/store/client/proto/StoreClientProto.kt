package sigma.software.leovegas.drugstore.store.client.proto

import feign.Headers
import feign.Param
import feign.RequestLine
import sigma.software.leovegas.drugstore.api.protobuf.Proto

@Headers("Content-Type: application/x-protobuf")
interface StoreClientProto {

    @RequestLine("GET /api/v1/store/check-transfer/{orderNumber}")
    fun checkTransfer(@Param orderNumber: String): Proto.CheckTransferResponse
}
