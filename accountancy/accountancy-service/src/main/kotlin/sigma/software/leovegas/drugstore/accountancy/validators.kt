package sigma.software.leovegas.drugstore.accountancy

import java.util.Optional
import sigma.software.leovegas.drugstore.accountancy.api.CreateOutcomeInvoiceRequest

fun CreateOutcomeInvoiceRequest.validate(functor: (Long) -> Optional<Invoice>): CreateOutcomeInvoiceRequest =
    apply {
        functor(orderId).ifPresent { throw OrderAlreadyConfirmedException(orderId) }
    }

fun Long.validate(functor: (Long) -> Optional<Invoice>): Invoice =
    run {
        functor(this).orElseThrow { InvoiceNotFoundException(this) }
    }
