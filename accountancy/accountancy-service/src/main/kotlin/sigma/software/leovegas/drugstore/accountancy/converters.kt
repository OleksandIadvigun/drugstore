package sigma.software.leovegas.drugstore.accountancy

import sigma.software.leovegas.drugstore.accountancy.api.ConfirmOrderResponse
import sigma.software.leovegas.drugstore.accountancy.api.InvoiceResponse
import sigma.software.leovegas.drugstore.accountancy.api.InvoiceStatusDTO
import sigma.software.leovegas.drugstore.accountancy.api.InvoiceTypeDTO
import sigma.software.leovegas.drugstore.accountancy.api.ProductItemDTO

// Invoice entity -> InvoiceResponse

fun Invoice.toConfirmOrderResponse() = ConfirmOrderResponse(
    orderNumber = orderNumber,
    amount = total,
)

// Invoice entity -> InvoiceResponseWithStatus

fun Invoice.toInvoiceResponseWithStatus() = InvoiceResponse(
    invoiceNumber = invoiceNumber,
    orderNumber = orderNumber,
    amount = total,
    status = status.toDTO()
)

fun List<Invoice>.toInvoiceResponseList() = this.map(Invoice::toConfirmOrderResponse)

// InvoiceStatus -> InvoiceStatusDTO

fun InvoiceStatus.toDTO(): InvoiceStatusDTO =
    InvoiceStatusDTO.valueOf(name)

// InvoiceType -> InvoiceTypeDTO

fun InvoiceType.toDTO(): InvoiceTypeDTO =
    InvoiceTypeDTO.valueOf(name)

// ProductItem entity -> ProductItemDTO

fun ProductItem.toDTO() = ProductItemDTO(
    productNumber = productNumber,
    name = name,
    price = price,
    quantity = quantity
)

fun Set<ProductItem>.toDTOs(): Set<ProductItemDTO> = this.map { it.toDTO() }.toSet()

