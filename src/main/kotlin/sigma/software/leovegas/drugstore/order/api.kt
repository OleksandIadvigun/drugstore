package sigma.software.leovegas.drugstore.order

import java.math.BigDecimal

data class OrderRequest(
    val orderDetailsList: List<OrderDetailsRequest>
)

data class OrderResponse(
    val id: Long? = null,
    val orderDetailsList: List<OrderDetailsResponse>,
)

data class OrderDetailsRequest(
    val productId: Long?,
    val quantity: Int
)

data class OrderDetailsResponse(
    val productId: Long?,
    val name: String,
    val price: BigDecimal,
    val quantity: Int
)

data class OrderInvoice(
    val orderId: Long?,
    val total: BigDecimal
)
