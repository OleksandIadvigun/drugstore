package sigma.software.leovegas.drugstore.order

import java.math.BigDecimal
import sigma.software.leovegas.drugstore.product.Product

fun Order.toOrderResponse(): OrderResponse =
    OrderResponse(
        id,
        orderDetailsList
            .orEmpty()
            .map {
                it.toOrderDetailsResponse()
            },
//        total = orderDetailsList
//            .orEmpty()
//            .map {
//                val price = it.product?.price ?: throw RuntimeException("price may not be null")
//                val quantity = it.quantity ?: throw RuntimeException("quantity may not be null")
//                price.multiply(BigDecimal(quantity))
//            }
//            .reduce(BigDecimal::plus)
//            .setScale(2, HALF_EVEN)
    total = total!!
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
        product?.id ?: -1,
        product?.name ?: "Invalid",
        product?.price ?: BigDecimal.ZERO,
        quantity ?: -1
    )

fun OrderDetailsRequest.toOrderDetails(): OrderDetails =
    OrderDetails(
        product = Product(
            id = productId
        ),
        quantity = quantity
    )

fun OrderDetailsResponse.toEntity(): OrderDetails =
    OrderDetails(
        product = Product(
            id = productId,
            name = name,
            price = price
        ),
        quantity = quantity
    )

fun List<OrderDetailsResponse>.toEntity(): List<OrderDetails> {
    return this.map(OrderDetailsResponse::toEntity)
}

fun List<OrderDetailsRequest>.toOrderDetails(): List<OrderDetails> {
    return this.map(OrderDetailsRequest::toOrderDetails)
}

fun List<OrderDetails>.toOrderDetailsResponse(): List<OrderDetailsResponse> {
    return this.map(OrderDetails::toOrderDetailsResponse)
}
