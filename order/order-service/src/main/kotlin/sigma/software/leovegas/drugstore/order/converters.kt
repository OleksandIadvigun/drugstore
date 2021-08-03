package sigma.software.leovegas.drugstore.order


fun Order.toOrderResponse(): OrderResponse = OrderResponse(id, orderItems)

fun OrderResponse.toEntity(): Order = Order(id, orderItems)

fun OrderRequest.toOrder(): Order = Order(orderItems= orderItems)

fun List<Order>.toOrderResponseList(): List<OrderResponse> = this.map(Order::toOrderResponse)



