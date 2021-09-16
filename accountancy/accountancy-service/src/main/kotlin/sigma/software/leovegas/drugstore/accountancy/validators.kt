package sigma.software.leovegas.drugstore.accountancy

import java.util.Optional
import java.util.UUID
import sigma.software.leovegas.drugstore.accountancy.api.CreateIncomeInvoiceRequest
import sigma.software.leovegas.drugstore.accountancy.api.CreateOutcomeInvoiceRequest

fun CreateOutcomeInvoiceRequest.validate(functor: (String, InvoiceStatus) -> Optional<Invoice>): CreateOutcomeInvoiceRequest =
    apply {
        functor(orderNumber, InvoiceStatus.CREATED).ifPresent {
            throw OrderAlreadyConfirmedException(orderNumber)
        }
        if (productItems.isEmpty()) throw ProductsItemsAreEmptyException()
    }

fun String.validate(functor: (String) -> Optional<Invoice>): Invoice =
    run {
        functor(this).orElseThrow { InvoiceNotFoundException(this) }
    }

fun List<String>.validate(): List<String> =
    onEach { if (it.isBlank()) throw ProductIdCannotBeNullException() }

fun CreateIncomeInvoiceRequest.validate(): CreateIncomeInvoiceRequest = apply {
    if (productItems.isEmpty()) throw ProductsItemsAreEmptyException()
}
