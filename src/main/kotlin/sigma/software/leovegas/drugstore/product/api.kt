package sigma.software.leovegas.drugstore.product

import java.math.BigDecimal

data class ProductRequest(
    val id: Long? = null,
    val name: String,
    val price: BigDecimal
)

data class ProductResponse(
    val id: Long? = null,
    val name: String,
    val price: BigDecimal
)
