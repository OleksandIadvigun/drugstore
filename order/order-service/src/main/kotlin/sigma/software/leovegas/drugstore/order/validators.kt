package sigma.software.leovegas.drugstore.order

import java.util.Optional
import sigma.software.leovegas.drugstore.order.api.CreateOrderRequest
import sigma.software.leovegas.drugstore.order.api.UpdateOrderRequest

fun CreateOrderRequest.validate() = apply {
    if (orderItems.isEmpty()) throw InsufficientAmountOfOrderItemException()
    orderItems.forEach {
        if (it.productNumber.isBlank() || it.quantity <= 0) {
            throw OrderRequestException("Not correct product number or quantity")
        }
    }
}

fun UpdateOrderRequest.validate() = apply {
    if (orderItems.isEmpty()) throw InsufficientAmountOfOrderItemException()
    orderItems.forEach {
        if (it.productNumber.isBlank() || it.quantity <= 0) {
            throw OrderRequestException("Not correct product number or quantity")
        }
    }
}

fun String.validate(functor: (String) -> Optional<Order>): Order =
    run {
        functor(this).orElseThrow { OrderNotFoundException(this) }
    }
