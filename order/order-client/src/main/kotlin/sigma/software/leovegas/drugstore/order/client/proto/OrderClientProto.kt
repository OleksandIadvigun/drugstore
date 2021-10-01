package sigma.software.leovegas.drugstore.order.client.proto

import feign.Headers
import feign.RequestLine
import sigma.software.leovegas.drugstore.api.protobuf.Proto

@Headers("Content-Type: application/x-protobuf")
interface OrderClientProto {

    @RequestLine("GET /api/v1/orders/total-buys")
    fun getProductsIdToQuantity(): Proto.ProductQuantityMap
}
