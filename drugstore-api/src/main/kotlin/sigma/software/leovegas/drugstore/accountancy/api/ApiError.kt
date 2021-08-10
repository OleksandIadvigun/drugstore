package sigma.software.leovegas.drugstore.accountancy.api

import java.time.LocalDateTime

data class ApiError(
    val code: Int = -1,
    val status: String = "Undefined",
    val message: String? = "Error has not been defined.",
    val time: LocalDateTime = LocalDateTime.now()
)
