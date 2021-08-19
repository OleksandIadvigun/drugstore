package sigma.software.leovegas.drugstore.accountancy

import sigma.software.leovegas.drugstore.accountancy.api.PriceItemRequest
import sigma.software.leovegas.drugstore.accountancy.api.PriceItemResponse

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
