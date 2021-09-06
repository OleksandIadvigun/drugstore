package sigma.software.leovegas.drugstore.accountancy.api

import java.math.BigDecimal
import java.time.LocalDateTime

// Request

data class CreateIncomeInvoiceRequest(
    val productItems: List<ProductItemDtoRequest>
)

data class CreateOutcomeInvoiceRequest(
    val productItems: List<ItemDTO>,
    val orderId: Long
)

data class MarkupUpdateRequest(
    val priceItemId: Long = -1,
    val markup: BigDecimal = BigDecimal.ZERO
)

// Response

data class MarkupUpdateResponse(
    val priceItemId: Long = -1,
    val price: BigDecimal = BigDecimal.ZERO,
    val markup: BigDecimal = BigDecimal.ZERO
)

data class InvoiceResponse(
    val id: Long = -1,
    val orderId: Long = -1,
    val type: InvoiceTypeDTO = InvoiceTypeDTO.NONE,
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

enum class InvoiceTypeDTO {
    NONE,
    INCOME,
    OUTCOME
}

data class ProductItemDtoRequest(
    val name: String = "default",
    val price: BigDecimal = BigDecimal.ZERO,
    val quantity: Int = 0
)

data class ProductItemDTO(
    val productId: Long = -1,
    val name: String = "default",
    val price: BigDecimal = BigDecimal.ZERO,
    val quantity: Int = 0
)

data class ItemDTO(
    val productId: Long = -1,
    val quantity: Int = 0
)

