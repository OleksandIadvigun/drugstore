package sigma.software.leovegas.drugstore.accountancy

import java.util.Optional
import sigma.software.leovegas.drugstore.accountancy.api.CreateIncomeInvoiceRequest
import sigma.software.leovegas.drugstore.accountancy.api.CreateOutcomeInvoiceRequest
import sigma.software.leovegas.drugstore.order.api.OrderStatusDTO

fun CreateOutcomeInvoiceRequest.validate(functor: (Long, InvoiceStatus) -> Optional<Invoice>): CreateOutcomeInvoiceRequest =
    apply {
        functor(orderNumber, InvoiceStatus.CREATED).ifPresent {
            throw OrderAlreadyConfirmedException(orderNumber)
        }
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
