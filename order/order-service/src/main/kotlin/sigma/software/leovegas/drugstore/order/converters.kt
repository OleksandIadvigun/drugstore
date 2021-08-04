package sigma.software.leovegas.drugstore.order

fun Order.toCreateOrderResponse(): CreateOrderResponse = CreateOrderResponse(
    id, orderStatus, createdAt, updatedAt, orderItems.toOrderItemViewSet()
)
fun Order.toUpdateOrderResponse(): UpdateOrderResponse = UpdateOrderResponse(
    id, orderStatus, createdAt, updatedAt, orderItems.toOrderItemViewSet()
)

fun CreateOrderResponse.toEntity(): Order = Order(id, orderStatus, createdAt, updateAt, orderItems.toOrderItemSet())

fun CreateOrderRequest.toOrder(): Order = Order(
    orderItems = orderItems.toOrderItemSet(),
    orderStatus = OrderStatus.CREATED
)
fun UpdateOrderRequest.toOrder(): Order = Order(
    orderItems = orderItems.toOrderItemSet(),
    orderStatus = OrderStatus.UPDATED
)

fun List<Order>.toOrderResponseList(): List<CreateOrderResponse> = this.map(Order::toCreateOrderResponse)

fun OrderItem.toOrderItemView(): OrderItemDto = OrderItemDto(productId = productId, quantity = quantity)

fun Set<OrderItem>.toOrderItemViewSet(): Set<OrderItemDto> = this.map(OrderItem::toOrderItemView).toSet()

fun Set<OrderItemDto>.toOrderItemSet(): Set<OrderItem> = this.map(OrderItemDto::toOrderItem).toSet()

fun OrderItemDto.toOrderItem(): OrderItem = OrderItem(productId = productId, quantity = quantity)




