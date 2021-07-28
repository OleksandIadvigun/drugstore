package sigma.software.leovegas.drugstore.order

import java.math.BigDecimal
import sigma.software.leovegas.drugstore.product.Product

fun Order.toOrderResponse(): OrderResponse =
    OrderResponse(
        id,
        orderDetailsList
            .map {
                it.toOrderDetailsResponse()
            },
    total = total
    )

fun OrderResponse.toEntity(): Order =
    Order(
        id = id,
        orderDetailsList = orderDetailsList.toEntity(),
        total = total
    )

fun List<Order>.toOrderResponseList(): List<OrderResponse> {
    return this.map(Order::toOrderResponse)
}

fun OrderDetails.toOrderDetailsResponse(): OrderDetailsResponse =
    OrderDetailsResponse(
        product.id,
        product.name,
        product.price ?: BigDecimal.ZERO,
        quantity
    )

fun OrderDetailsResponse.toEntity(): OrderDetails =
    OrderDetails(
        product = Product(
            id = productId,
            name = name,
            price = price,
            quantity = 0
        ),
        quantity = quantity
    )

fun List<OrderDetailsResponse>.toEntity(): List<OrderDetails> {
    return this.map(OrderDetailsResponse::toEntity)
}

