package sigma.software.leovegas.drugstore.accountancy.api

import java.math.BigDecimal

// Request

data class CreateIncomeInvoiceRequest(
    val productItems: List<ProductItemDtoRequest>
)

data class CreateOutcomeInvoiceRequest(
    val productItems: List<ItemDTO>,
    val orderNumber: Long
)

// Response

data class ConfirmOrderResponse(
    val orderNumber: Long = -1,
    val amount: BigDecimal = BigDecimal.ZERO,
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

