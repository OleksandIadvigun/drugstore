package sigma.software.leovegas.drugstore.product.client

import feign.Headers
import feign.Param
import feign.RequestLine
import sigma.software.leovegas.drugstore.product.api.ProductRequest
import sigma.software.leovegas.drugstore.product.api.ProductResponse

@Headers("Content-Type: application/json")
interface ProductClient {

    @RequestLine("POST api/v1/products")
    fun createProduct(request: ProductRequest): ProductResponse

    @RequestLine("GET api/v1/products")
    fun getProducts(): List<ProductResponse>

    @RequestLine("GET api/v1/products/{id}")
    fun getProductById(@Param id: Long): ProductResponse

    @RequestLine("PUT api/v1/products/{id}")
    fun updateProduct(@Param id: Long, request: ProductRequest): ProductResponse

    @RequestLine("DELETE api/v1/products/{id}")
    fun deleteProduct(@Param id: Long)
}