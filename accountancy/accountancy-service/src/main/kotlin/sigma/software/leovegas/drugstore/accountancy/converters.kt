package sigma.software.leovegas.drugstore.accountancy

import sigma.software.leovegas.drugstore.accountancy.api.InvoiceResponse
import sigma.software.leovegas.drugstore.accountancy.api.InvoiceStatusDTO
import sigma.software.leovegas.drugstore.accountancy.api.PriceItemRequest
import sigma.software.leovegas.drugstore.accountancy.api.PriceItemResponse
import sigma.software.leovegas.drugstore.accountancy.api.ProductItemDTO

// PriceItemRequest -> PriceItem entity

fun PriceItemRequest.toEntity() = PriceItem(
    productId = productId,
    price = price
)

// PriceItem entity -> PriceResponse

fun PriceItem.toPriceItemResponse() = PriceItemResponse(
    id = id ?: -1,
    productId = productId ?: -1,
    price = price,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun List<PriceItem>.toPriceItemResponseList() = this.map(PriceItem::toPriceItemResponse)

// Invoice entity -> InvoiceResponse

fun Invoice.toInvoiceResponse() = InvoiceResponse(
    id = id ?: -1,
    orderId = orderId ?: -1,
    status = status.toDTO(),
    productItems = productItems.toDTOs(),
    total = total,
    createdAt = createdAt,
    expiredAt = createdAt,
)

fun InvoiceStatus.toDTO(): InvoiceStatusDTO =
    InvoiceStatusDTO.valueOf(name)

fun ProductItem.toDTO() = ProductItemDTO(
    priceItemId = priceItemId ?: -1,
    name = name,
    price = price,
    quantity = quantity
)

fun Set<ProductItem>.toDTOs(): Set<ProductItemDTO> = this.map { it.toDTO() }.toSet()