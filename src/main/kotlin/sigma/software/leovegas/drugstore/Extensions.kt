package sigma.software.leovegas.drugstore

import sigma.software.leovegas.drugstore.dto.OrderDetailsRequest
import sigma.software.leovegas.drugstore.dto.OrderDetailsResponse
import sigma.software.leovegas.drugstore.dto.OrderRequest
import sigma.software.leovegas.drugstore.dto.OrderResponse
import sigma.software.leovegas.drugstore.persistence.entity.OrderDetails
import sigma.software.leovegas.drugstore.persistence.entity.Order
import sigma.software.leovegas.drugstore.persistence.entity.Product

fun OrderRequest.toEntity(): Order =
    Order(
        orderDetailsList = orderDetailsList.toOrderDetails()
    )

fun Order.toOrderResponse(): OrderResponse =
    OrderResponse(
        id = id!!,
        orderDetailsList = orderDetailsList?.toOrderDetailsResponse() ?: listOf(),
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
        productId = product!!.id!!,
        name = product.name!!,
        price = product.price!!,
        quantity = quantity!!
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
