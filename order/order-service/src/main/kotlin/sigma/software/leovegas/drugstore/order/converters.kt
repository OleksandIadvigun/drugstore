package sigma.software.leovegas.drugstore.order

import sigma.software.leovegas.drugstore.order.api.CreateOrderRequest
import sigma.software.leovegas.drugstore.order.api.CreateOrderResponse
import sigma.software.leovegas.drugstore.order.api.OrderItemDTO
import sigma.software.leovegas.drugstore.order.api.OrderStatusDTO
import sigma.software.leovegas.drugstore.order.api.UpdateOrderResponse

// CreateOrderRequest <-> Order entity

fun CreateOrderRequest.toEntity(): Order =
    Order(orderItems = orderItems.toEntities())

// CreateOrderResponse <-> Order entity

fun Order.toCreateOrderResponseDTO(): CreateOrderResponse =
    CreateOrderResponse(
        id = id ?: -1,
        orderStatus = orderStatus.toDTO(),
        orderItems = orderItems.toDTOs(),
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

fun CreateOrderResponse.toEntity(): Order =
    Order(
        id = id,
        orderStatus = orderStatus.toEntity(),
        orderItems = orderItems.toEntities(),
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

// Order entity -> UpdateOrderResponse

fun Order.toUpdateOrderResponseDTO(): UpdateOrderResponse =
    UpdateOrderResponse(
        id = id ?: -1,
        orderStatus = orderStatus.toDTO(),
        orderItems = orderItems.toDTOs(),
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

fun List<Order>.toOrderResponseList(): List<CreateOrderResponse> = this.map(Order::toCreateOrderResponseDTO)

// OrderItem: set of entities <-> list of DTOs

fun Set<OrderItem>.toDTOs(): List<OrderItemDTO> =
    map(OrderItem::toDTO).toList()

fun List<OrderItemDTO>.toEntities(): Set<OrderItem> =
    map(OrderItemDTO::toEntity).toSet()

// OrderItem: entity <-> DTO

fun OrderItem.toDTO(): OrderItemDTO =
    OrderItemDTO(
        productId = productId,
        quantity = quantity,
    )

fun OrderItemDTO.toEntity(): OrderItem =
    OrderItem(
        productId = productId,
        quantity = quantity,
    )

// OrderStatus: entity <-> DTO

fun OrderStatus.toDTO(): OrderStatusDTO =
    OrderStatusDTO.valueOf(name)

fun OrderStatusDTO.toEntity(): OrderStatus =
    OrderStatus.valueOf(name)



