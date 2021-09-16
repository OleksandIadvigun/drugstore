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
    val orderNumber: String = "undefined",
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
}

data class OrderItemDTO(
    val productNumber: String = "undefined",
    val quantity: Int = -1,
)

data class OrderDetailsDTO(
    val orderNumber: String = "undefined",
    val orderItemDetails: List<OrderItemDetailsDTO> = listOf(),
    val total: BigDecimal = BigDecimal("-1"),
)

data class OrderItemDetailsDTO(
    val productNumber: String = "undefined",
    val name: String = "undefined",
    val price: BigDecimal = BigDecimal("-1"),
    val quantity: Int = -1,
)
