package sigma.software.leovegas.drugstore.exception

import java.time.LocalDateTime

data class ApiExceptionModel(
    var message: String? = "",
    val time: LocalDateTime? = null
)
