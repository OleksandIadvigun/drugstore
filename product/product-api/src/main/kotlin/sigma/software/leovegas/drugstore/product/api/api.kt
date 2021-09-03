package sigma.software.leovegas.drugstore.product.api

import java.math.BigDecimal
import java.time.LocalDateTime

// Request

data class CreateProductRequest(
    val name: String = "undefined",
    val quantity: Int = 0,
    val price: BigDecimal = BigDecimal.ZERO,
)

data class DeliverProductsQuantityRequest(
    val id: Long = -1,
    val quantity: Int = 0,
)

data class ReturnProductQuantityRequest(
    val id: Long = -1,
    val quantity: Int = 0,
)

// Response

data class SearchProductResponse(
    val id: Long = -1,
    val name: String = "undefined",
    val price: BigDecimal = BigDecimal.ZERO,
    val quantity: Int = 0,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
)

data class ProductDetailsResponse(
    val id: Long = -1,
    val name: String = "undefined",
    val price: BigDecimal = BigDecimal.ZERO,
    val quantity: Int = -1,
)

data class GetProductResponse(
    val id: Long = -1,
    val name: String = "undefined",
)

data class CreateProductResponse(
    val id: Long = -1,
    val status: ProductStatusDTO = ProductStatusDTO.NONE,
    val name: String = "undefined",
    val price: BigDecimal = BigDecimal.ZERO,
    val quantity: Int = 0,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
)

data class DeliverProductsResponse(
    val id: Long = -1,
    val quantity: Int = 0,
    val updatedAt: LocalDateTime? = null
)

data class ReceiveProductResponse(
    val id: Long = -1,
    val status: ProductStatusDTO = ProductStatusDTO.NONE
)

data class ReturnProductsResponse(
    val id: Long = -1,
    val quantity: Int = 0,
    val updatedAt: LocalDateTime? = null
)

// DTOs

enum class ProductStatusDTO {
    CREATED, RECEIVED, NONE
}
