package sigma.software.leovegas.drugstore.store

import java.util.Optional

fun Long.validate(functor: (Long) -> Optional<TransferCertificate>): Long =
    apply {
        functor(this).ifPresent { throw ProductsAlreadyDelivered(this) }
    }
