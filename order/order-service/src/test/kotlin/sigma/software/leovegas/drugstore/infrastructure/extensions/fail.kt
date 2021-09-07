package sigma.software.leovegas.drugstore.infrastructure.extensions

import org.junit.jupiter.api.fail

infix fun <T> T?.get(name: String): T =
    this ?: fail("$name may not be null.")

fun <T : Any> T?.get(): T =
    this get "result"
