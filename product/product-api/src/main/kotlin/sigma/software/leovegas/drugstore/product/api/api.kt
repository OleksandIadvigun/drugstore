package sigma.software.leovegas.drugstore.product.api

import java.math.BigDecimal
import java.time.LocalDateTime

// Request

data class CreateProductRequest(
    val productNumber: String = "undefined",
    val name: String = "undefined",
    val quantity: Int = 0,
    val price: BigDecimal = BigDecimal.ZERO,
)

data class CreateProductsEvent(
    val list: List<CreateProductRequest> = listOf()
)

data class DeliverProductsQuantityRequest(
    val productNumber: String = "undefined",
    val quantity: Int = 0,
)

// Response

data class SearchProductResponse(
    val productNumber: String = "undefined",
    val name: String = "undefined",
    val price: BigDecimal = BigDecimal.ZERO,
    val quantity: Int = 0,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
)

data class ProductDetailsResponse(
    val productNumber: String = "undefined",
    val name: String = "undefined",
    val price: BigDecimal = BigDecimal.ZERO,
    val quantity: Int = -1,
)

data class GetProductResponse(
    val productNumber: String = "undefined",
    val name: String = "undefined",
)

data class CreateProductResponse(
    val productNumber: String = "undefined",
    val status: ProductStatusDTO = ProductStatusDTO.NONE,
    val name: String = "undefined",
    val price: BigDecimal = BigDecimal.ZERO,
    val quantity: Int = 0,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
)

// DTOs

enum class ProductStatusDTO {
    CREATED, RECEIVED, NONE
}
