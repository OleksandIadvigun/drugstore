package sigma.software.leovegas.drugstore.accountancy

import sigma.software.leovegas.drugstore.accountancy.api.InvoiceResponse
import sigma.software.leovegas.drugstore.accountancy.api.InvoiceStatusDTO
import sigma.software.leovegas.drugstore.accountancy.api.InvoiceTypeDTO
import sigma.software.leovegas.drugstore.accountancy.api.ProductItemDTO


// Invoice entity -> InvoiceResponse

fun Invoice.toInvoiceResponse() = InvoiceResponse(
    id = id ?: -1,
    orderId = orderId ?: -1,
    type = type.toDTO(),
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

// InvoiceType -> InvoiceTypeDTO

fun InvoiceType.toDTO(): InvoiceTypeDTO =
    InvoiceTypeDTO.valueOf(name)

// ProductItem entity -> ProductItemDTO

fun ProductItem.toDTO() = ProductItemDTO(
    productId = productId ?: -1,
    name = name,
    price = price,
    quantity = quantity
)

fun Set<ProductItem>.toDTOs(): Set<ProductItemDTO> = this.map { it.toDTO() }.toSet()

//// PriceItem entity -> MarkupUpdateResponse
//
//fun PriceItem.toMarkupUpdateResponse(): MarkupUpdateResponse = MarkupUpdateResponse(
//    priceItemId = id ?: -1,
//    price = price,
//    markup = markup
//)
//
//fun List<PriceItem>.toMarkupUpdateResponse(): List<MarkupUpdateResponse> = this.map(PriceItem::toMarkupUpdateResponse)
