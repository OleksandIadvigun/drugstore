package sigma.software.leovegas.drugstore.accountancy.api

import java.math.BigDecimal
import java.time.LocalDateTime

// Request

data class PriceItemRequest(
    val productId: Long = -1,
    val price: BigDecimal = BigDecimal.ZERO
)

// Response

data class PriceItemResponse(
    val id: Long = -1,
    val productId: Long = -1,
    val price: BigDecimal = BigDecimal.ZERO,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
)

