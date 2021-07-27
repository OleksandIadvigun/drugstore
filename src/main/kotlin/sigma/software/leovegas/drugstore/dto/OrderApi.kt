package sigma.software.leovegas.drugstore.dto

import java.math.BigDecimal


data class OrderRequest(
    val orderDetailsList: List<OrderDetailsRequest> = listOf()
)

data class OrderResponse(
    val id: Long = 0L,
    val orderDetailsList: List<OrderDetailsResponse> = listOf(),
    val total: BigDecimal = BigDecimal.ZERO
)

data class OrderDetailsRequest(
    val productId: Long = 0L,
    val quantity: Int = 0
)

data class OrderDetailsResponse(
    val productId: Long = 0L,
    val name: String = "",
    val price: BigDecimal = BigDecimal.ZERO,
    val quantity: Int = 0
)
