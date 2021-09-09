package sigma.software.leovegas.drugstore.product

import java.util.Optional

fun Long.validate(functor: (Long) -> Optional<Product>): Product =
    run {
        functor(this).orElseThrow { ResourceNotFoundException(this) }
    }

fun <T> List<T>.validate() = apply {
    if (isEmpty()) throw NotCorrectRequestException("Should not be empty request list")
}
