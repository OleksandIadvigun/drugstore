package sigma.software.leovegas.drugstore.accountancy

import sigma.software.leovegas.drugstore.accountancy.api.InvoiceResponse
import sigma.software.leovegas.drugstore.accountancy.api.InvoiceStatusDTO
import sigma.software.leovegas.drugstore.accountancy.api.PriceItemRequest
import sigma.software.leovegas.drugstore.accountancy.api.PriceItemResponse
import sigma.software.leovegas.drugstore.accountancy.api.ProductItemDTO
import sigma.software.leovegas.drugstore.accountancy.api.PurchasedCostsRequest
import sigma.software.leovegas.drugstore.accountancy.api.PurchasedCostsResponse

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

fun List<Invoice>.toInvoiceResponseList() = this.map(Invoice::toInvoiceResponse)

// InvoiceStatus -> InvoiceStatusDTO

fun InvoiceStatus.toDTO(): InvoiceStatusDTO =
    InvoiceStatusDTO.valueOf(name)

// ProductItem entity -> ProductItemDTO

fun ProductItem.toDTO() = ProductItemDTO(
    priceItemId = priceItemId ?: -1,
    name = name,
    price = price,
    quantity = quantity
)

fun Set<ProductItem>.toDTOs(): Set<ProductItemDTO> = this.map { it.toDTO() }.toSet()

// PurchasedCostsRequest -> PurchasedCosts entity

fun PurchasedCostsRequest.toEntity() = PurchasedCosts(
    priceItemId = priceItemId,
    quantity = quantity
)

// PurchasedCosts entity -> PurchasedCostsResponse

fun PurchasedCosts.toPurchasedCostsResponse() = PurchasedCostsResponse(
    id = id ?: -1,
    priceItemId = priceItemId,
    quantity = quantity,
    dateOfPurchase = dateOfPurchase
)

fun List<PurchasedCosts>.toPurchasedCostsResponseList() = this.map(PurchasedCosts::toPurchasedCostsResponse)
