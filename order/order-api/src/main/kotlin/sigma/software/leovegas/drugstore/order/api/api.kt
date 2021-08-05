package sigma.software.leovegas.drugstore.order.api

import java.time.LocalDateTime
import sigma.software.leovegas.drugstore.order.api.OrderStatusDTO.NONE

// Requests

data class CreateOrderRequest(
    val orderItems: List<OrderItemDTO> = listOf()
)

data class UpdateOrderRequest(
    val orderItems: List<OrderItemDTO> = listOf()
)

// Responses

data class CreateOrderResponse(
    val id: Long = -1,
    val orderStatus: OrderStatusDTO = NONE,
    val orderItems: List<OrderItemDTO> = listOf(),
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
)

data class UpdateOrderResponse(
    val id: Long = -1,
    val orderStatus: OrderStatusDTO = NONE,
    val orderItems: List<OrderItemDTO> = listOf(),
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
)

// DTOs

enum class OrderStatusDTO {
    NONE,
    CREATED,
    CANCELLED,
    UPDATED,
    PAID,
    DELIVERED
}

data class OrderItemDTO(
    val productId: Long = -1,
    val quantity: Int = -1,
)
