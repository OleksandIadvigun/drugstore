package sigma.software.leovegas.drugstore.accountancy

import java.util.Optional
import sigma.software.leovegas.drugstore.accountancy.api.CreateIncomeInvoiceRequest
import sigma.software.leovegas.drugstore.accountancy.api.CreateOutcomeInvoiceRequest

fun CreateOutcomeInvoiceRequest.validate(functor: (Long) -> Optional<Invoice>): CreateOutcomeInvoiceRequest =
    apply {
        functor(orderId).ifPresent { throw OrderAlreadyConfirmedException(orderId) }
        if (productItems.isEmpty()) throw ProductsItemsAreEmptyException()
    }

fun Long.validate(functor: (Long) -> Optional<Invoice>): Invoice =
    run {
        functor(this).orElseThrow { InvoiceNotFoundException(this) }
    }

fun List<Long>.validate(): List<Long> =
    onEach { if (it < 1) throw ProductIdCannotBeNullException() }

fun CreateIncomeInvoiceRequest.validate(): CreateIncomeInvoiceRequest = apply {
    if (productItems.isEmpty()) throw ProductsItemsAreEmptyException()
}
