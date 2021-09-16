package sigma.software.leovegas.drugstore.accountancy.api

import java.math.BigDecimal

// Request

data class CreateIncomeInvoiceRequest(
    val productItems: List<ProductItemDtoRequest>
)

data class CreateOutcomeInvoiceEvent(
    val productItems: List<ItemDTO>,
    val orderNumber: String
)

// Response

data class ConfirmOrderResponse(
    val orderNumber: String = "undefined",
    val amount: BigDecimal = BigDecimal.ZERO,
)

data class InvoiceResponse(
    val invoiceNumber: String = "undefined",
    val orderNumber: String = "undefined",
    val amount: BigDecimal = BigDecimal.ZERO,
    val status: InvoiceStatusDTO = InvoiceStatusDTO.NONE
)
// DTOs

enum class InvoiceStatusDTO {
    NONE,
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
    val productNumber: String = "default",
    val name: String = "default",
    val price: BigDecimal = BigDecimal.ZERO,
    val quantity: Int = 0
)

data class ItemDTO(
    val productNumber: String = "default",
    val quantity: Int = 0
)

