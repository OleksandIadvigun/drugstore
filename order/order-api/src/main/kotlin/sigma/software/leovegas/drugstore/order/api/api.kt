package sigma.software.leovegas.drugstore.order.api

import java.math.BigDecimal
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

data class OrderResponse(
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
    CONFIRMED,
    REFUND,
    PAID,
    BOOKED,
    DELIVERED,
}

data class OrderItemDTO(
    val productId: Long = -1,
    val quantity: Int = -1,
)

data class OrderDetailsDTO(
    val orderItemDetails: List<OrderItemDetailsDTO> = listOf(),
    val total: BigDecimal = BigDecimal("-1"),
)

data class OrderItemDetailsDTO(
    val productId: Long = -1,
    val name: String = "undefined",
    val price: BigDecimal = BigDecimal("-1"),
    val quantity: Int = -1,
)
