package sigma.software.leovegas.drugstore.order

import sigma.software.leovegas.drugstore.order.api.CreateOrderEvent
import sigma.software.leovegas.drugstore.order.api.OrderItemDTO
import sigma.software.leovegas.drugstore.order.api.OrderResponse
import sigma.software.leovegas.drugstore.order.api.OrderStatusDTO

// CreateOrderRequest <-> Order entity

fun CreateOrderEvent.toEntity(): Order =
    Order(
        orderItems = orderItems.toEntities(),
        orderNumber = orderNumber
    )

// OrderResponse <-> Order entity

fun Order.toOrderResponseDTO(): OrderResponse =
    OrderResponse(
        orderNumber = orderNumber,
        orderStatus = orderStatus.toDTO(),
        orderItems = orderItems.toDTOs(),
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

fun OrderResponse.toEntity(): Order =
    Order(
        orderNumber = orderNumber,
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
        productNumber = productNumber,
        quantity = quantity,
    )

fun OrderItemDTO.toEntity(): OrderItem =
    OrderItem(
        productNumber = productNumber,
        quantity = quantity,
    )

// OrderStatus: entity <-> DTO

fun OrderStatus.toDTO(): OrderStatusDTO =
    OrderStatusDTO.valueOf(name)

fun OrderStatusDTO.toEntity(): OrderStatus =
    OrderStatus.valueOf(name)



