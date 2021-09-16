package sigma.software.leovegas.drugstore.store

import java.util.Optional
import sigma.software.leovegas.drugstore.product.api.DeliverProductsQuantityRequest
import sigma.software.leovegas.drugstore.store.api.TransferCertificateRequest
import sigma.software.leovegas.drugstore.store.api.TransferStatusDTO

fun String.validate(functor: (String) -> Optional<TransferCertificate>): String =
    apply {
        functor(this).ifPresent { throw ProductsAlreadyDelivered(this) }
    }

fun List<DeliverProductsQuantityRequest>.validate() =
    onEach { if (it.quantity <= 0) throw NotCorrectQuantityException() }

fun TransferCertificateRequest.validate() = apply {
    if (orderNumber == "undefined" || comment == "undefined" || status == TransferStatusDTO.NONE) {
        throw NotCorrectRequestException()
    }
}
