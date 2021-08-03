package sigma.software.leovegas.drugstore.infrastructure

import java.time.LocalDateTime

data class ApiError(
    val message: String? = "Error has not been defined.",
    val time: LocalDateTime = LocalDateTime.now()
)
