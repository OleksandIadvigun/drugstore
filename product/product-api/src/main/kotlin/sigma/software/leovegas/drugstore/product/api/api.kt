package sigma.software.leovegas.drugstore.product.api

import java.math.BigDecimal
import java.time.LocalDateTime

// Request

data class ProductRequest(
    val name: String = "undefined",
)

// Response

data class ProductResponse(
    val id: Long = -1,
    val name: String = "undefined",
    val totalBuys: Int = 0,
    val priceItemId: Long = -1,
    val price: BigDecimal = BigDecimal.ZERO,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
)
