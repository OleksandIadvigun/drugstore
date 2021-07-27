package sigma.software.leovegas.drugstore.dto

import java.math.BigDecimal

data class ProductResponse (
        var id: Long = 0,

        var name: String = "",

        var quantity: Int = 0,

        var price: BigDecimal? = null
    )

