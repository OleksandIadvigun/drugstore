package sigma.software.leovegas.drugstore.product.client.proto

import feign.Headers
import feign.Param
import feign.RequestLine
import sigma.software.leovegas.drugstore.api.protobuf.Proto

@Headers("Content-Type: application/x-protobuf")
interface ProductClientProto {

    @RequestLine("PUT /api/v1/products/receive")
    fun receiveProducts(productNumbers: Proto.ProductNumberList): Proto.ReceiveProductResponse

    @RequestLine("PUT /api/v1/products/deliver")
    fun deliverProducts(products: Proto.DeliverProductsDTO): Proto.DeliverProductsDTO

    @RequestLine("GET /api/v1/products/details?productNumbers={productNumbers}")
    fun getProductsDetailsByProductNumbers(@Param productNumbers: List<String>): Proto.ProductDetailsResponse
}
