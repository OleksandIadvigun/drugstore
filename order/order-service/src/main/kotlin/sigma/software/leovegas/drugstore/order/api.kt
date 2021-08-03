package sigma.software.leovegas.drugstore.order

import java.math.BigDecimal

data class OrderRequest(
    val orderItems: Set<OrderItem>
)

data class OrderResponse(
    val id: Long? = null,
    val orderItems: Set<OrderItem>,
)

data class OrderInvoice(
    val orderId: Long?,
    val total: BigDecimal
)
