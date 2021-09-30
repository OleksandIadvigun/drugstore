package sigma.software.leovegas.drugstore.product.client.proto

import feign.Headers
import feign.RequestLine
import org.springframework.cloud.openfeign.FeignClient
import sigma.software.leovegas.drugstore.api.protobuf.Proto

@Headers("Content-Type: application/x-protobuf")
@FeignClient(name = "FEIGN-PROTO", configuration = [ProductClientConfigurationProto::class])
interface ProductClientProto {

    @RequestLine("PUT /api/v1/products/receive")
    fun receiveProducts(productNumbers: Proto.ReceiveProductRequest): Proto.ReceiveProductResponse

    @RequestLine("PUT /api/v1/products/deliver")
    fun deliverProducts(products: Proto.DeliverProductsDTO): Proto.DeliverProductsDTO

}
