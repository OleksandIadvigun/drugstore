package sigma.software.leovegas.drugstore.dto

import java.math.BigDecimal

data class ProductRequest(
    var id: Long = 0,

    var name: String = "",

    var quantity: Int = 0,

    var price: BigDecimal? = null
)
