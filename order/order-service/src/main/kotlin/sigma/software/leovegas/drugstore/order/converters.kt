package sigma.software.leovegas.drugstore.order

import sigma.software.leovegas.drugstore.order.api.CreateOrderRequest
import sigma.software.leovegas.drugstore.order.api.OrderItemDTO
import sigma.software.leovegas.drugstore.order.api.OrderResponse
import sigma.software.leovegas.drugstore.order.api.OrderStatusDTO

// CreateOrderRequest <-> Order entity

fun CreateOrderRequest.toEntity(): Order =
    Order(orderItems = orderItems.toEntities())

// OrderResponse <-> Order entity

fun Order.toOrderResponseDTO(): OrderResponse =
    OrderResponse(
        orderNumber = id ?: -1,
        orderStatus = orderStatus.toDTO(),
        orderItems = orderItems.toDTOs(),
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

fun OrderResponse.toEntity(): Order =
    Order(
        id = orderNumber,
        orderStatus = orderStatus.toEntity(),
        orderItems = orderItems.toEntities(),
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

fun List<Order>.toOrderResponseList(): List<OrderResponse> = this.map(Order::toOrderResponseDTO)

// OrderItem: set of entities <-> list of DTOs

fun Set<OrderItem>.toDTOs(): List<OrderItemDTO> =
    map(OrderItem::toDTO).toList()

fun List<OrderItemDTO>.toEntities(): Set<OrderItem> =
    map(OrderItemDTO::toEntity).toSet()

// OrderItem: entity <-> DTO

fun OrderItem.toDTO(): OrderItemDTO =
    OrderItemDTO(
        productNumber = productId,
        quantity = quantity,
    )

fun OrderItemDTO.toEntity(): OrderItem =
    OrderItem(
        productId = productNumber,
        quantity = quantity,
    )

// OrderStatus: entity <-> DTO

fun OrderStatus.toDTO(): OrderStatusDTO =
    OrderStatusDTO.valueOf(name)

fun OrderStatusDTO.toEntity(): OrderStatus =
    OrderStatus.valueOf(name)



