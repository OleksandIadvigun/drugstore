package sigma.software.leovegas.drugstore.product.client

import feign.Headers
import feign.Param
import feign.RequestLine
import org.springframework.data.domain.Page
import sigma.software.leovegas.drugstore.product.api.CreateProductRequest
import sigma.software.leovegas.drugstore.product.api.CreateProductResponse
import sigma.software.leovegas.drugstore.product.api.GetProductResponse
import sigma.software.leovegas.drugstore.product.api.ProductDetailsResponse
import sigma.software.leovegas.drugstore.product.api.ReceiveProductResponse
import sigma.software.leovegas.drugstore.product.api.ReduceProductQuantityRequest
import sigma.software.leovegas.drugstore.product.api.ReduceProductQuantityResponse
import sigma.software.leovegas.drugstore.product.api.SearchProductResponse

@Headers("Content-Type: application/json")
interface ProductClient {


    @RequestLine("POST api/v1/products")
    fun createProduct(request: List<CreateProductRequest>): List<CreateProductResponse>

    @RequestLine(
        "GET api/v1/products/search?page={page}&size={size}&search={search}&sortField={sortField}&sortDirection={sortDirection}"
    )
    fun searchProducts(
        @Param("page") page: Int = 0,
        @Param("size") size: Int = 5,
        @Param("search") search: String = "",
        @Param("sortField") sortField: String = "popularity",
        @Param("sortDirection") sortDirection: String = "DESC"
    ): Page<SearchProductResponse>

    @RequestLine("GET api/v1/products/popular?page={page}&size={size}")
    fun getPopularProducts(@Param("page") page: Int = 0, @Param("size") size: Int = 5): Page<GetProductResponse>

    @RequestLine("PUT api/v1/products/receive")
    fun receiveProducts(ids: List<Long>): List<ReceiveProductResponse>

    @RequestLine("PUT api/v1/products/reduce-quantity")
    fun reduceQuantity(products: List<ReduceProductQuantityRequest>): List<ReduceProductQuantityResponse>

    @RequestLine("GET api/v1/products/details?ids={ids}")
    fun getProductsDetailsByIds(@Param ids: List<Long>): List<ProductDetailsResponse>
}
