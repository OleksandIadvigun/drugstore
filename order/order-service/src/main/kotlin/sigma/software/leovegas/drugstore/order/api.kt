package sigma.software.leovegas.drugstore.order

import java.time.LocalDateTime

data class CreateOrderRequest(
    val orderItems: Set<OrderItemDto>
)
data class UpdateOrderRequest(
    val orderItems: Set<OrderItemDto>
)

data class CreateOrderResponse(
    val id: Long? = null,
    val orderStatus: OrderStatus,
    val createdAt: LocalDateTime? = null,
    val updateAt: LocalDateTime? = null,
    val orderItems: Set<OrderItemDto>,
)

data class UpdateOrderResponse(
    val id: Long? = null,
    val orderStatus: OrderStatus,
    val createdAt: LocalDateTime? = null,
    val updateAt: LocalDateTime? = null,
    val orderItems: Set<OrderItemDto>,
)

data class OrderItemDto(
    val productId: Long,
    val quantity: Int,
)

