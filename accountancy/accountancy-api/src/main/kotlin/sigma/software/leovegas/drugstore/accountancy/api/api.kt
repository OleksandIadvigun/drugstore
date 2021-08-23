package sigma.software.leovegas.drugstore.accountancy.api

import java.math.BigDecimal
import java.time.LocalDateTime

// Request

data class PriceItemRequest(
    val productId: Long = -1,
    val price: BigDecimal = BigDecimal.ZERO
)

data class InvoiceRequest(
    val orderId: Long = -1,
)

// Response

data class PriceItemResponse(
    val id: Long = -1,
    val productId: Long = -1,
    val price: BigDecimal = BigDecimal.ZERO,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
)

data class InvoiceResponse(
    val id: Long = -1,
    val orderId: Long = -1,
    val status: InvoiceStatusDTO = InvoiceStatusDTO.CREATED,
    val productItems: Set<ProductItemDTO> = setOf(),
    val total: BigDecimal = BigDecimal.ZERO,
    val createdAt: LocalDateTime? = null,
    val expiredAt: LocalDateTime? = null,
)

// DTOs

enum class InvoiceStatusDTO {
    CREATED,
    CANCELLED,
    PAID,
    REFUND
}

data class ProductItemDTO(
    val priceItemId: Long = -1,
    val name: String = "default",
    val price: BigDecimal = BigDecimal.ZERO,
    val quantity: Int = 0
)



