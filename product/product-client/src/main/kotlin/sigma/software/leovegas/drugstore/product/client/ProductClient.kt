package sigma.software.leovegas.drugstore.product.client

import feign.Headers
import feign.Param
import feign.RequestLine
import java.math.BigDecimal
import sigma.software.leovegas.drugstore.product.api.CreateProductResponse
import sigma.software.leovegas.drugstore.product.api.CreateProductsEvent
import sigma.software.leovegas.drugstore.product.api.GetProductResponse
import sigma.software.leovegas.drugstore.product.api.ProductDetailsResponse
import sigma.software.leovegas.drugstore.product.api.SearchProductResponse

@Headers("Content-Type: application/json")
interface ProductClient {

    @RequestLine("POST /api/v1/products")
    fun createProduct(event: CreateProductsEvent): List<CreateProductResponse>

    @RequestLine(
        "GET /api/v1/products/search?page={page}&size={size}&search={search}&sortField={sortField}&sortDirection={sortDirection}"
    )
    fun searchProducts(
        @Param("page") page: Int = 0,
        @Param("size") size: Int = 5,
        @Param("search") search: String = "",
        @Param("sortField") sortField: String = "popularity",
        @Param("sortDirection") sortDirection: String = "DESC"
    ): List<SearchProductResponse>

    @RequestLine("GET /api/v1/products/popular?page={page}&size={size}")
    fun getPopularProducts(@Param("page") page: Int = 0, @Param("size") size: Int = 5): List<GetProductResponse>

    @RequestLine("GET /api/v1/products/{productNumber}/price")
    fun getProductPrice(@Param productNumber: List<String>): Map<String, BigDecimal>

    @RequestLine("GET /api/v1/products/details/{productNumber}")
    fun getProductsDetailsByProductNumbers(@Param productNumber: String): ProductDetailsResponse
}
