package sigma.software.leovegas.drugstore.accountancy

import sigma.software.leovegas.drugstore.accountancy.api.ConfirmOrderResponse
import sigma.software.leovegas.drugstore.accountancy.api.InvoiceResponse
import sigma.software.leovegas.drugstore.accountancy.api.InvoiceStatusDTO

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

// InvoiceStatus -> InvoiceStatusDTO

fun InvoiceStatus.toDTO(): InvoiceStatusDTO =
    InvoiceStatusDTO.valueOf(name)
