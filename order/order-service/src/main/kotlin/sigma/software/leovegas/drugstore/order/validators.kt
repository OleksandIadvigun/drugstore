package sigma.software.leovegas.drugstore.order

import java.util.Optional
import sigma.software.leovegas.drugstore.order.api.CreateOrderRequest
import sigma.software.leovegas.drugstore.order.api.UpdateOrderRequest

fun CreateOrderRequest.validate() = apply {
    if (orderItems.isEmpty()) throw InsufficientAmountOfOrderItemException()
    orderItems.forEach {
        if (it.productNumber < 0 || it.quantity <= 0) {
            throw OrderRequestException("Not correct product number or quantity")
        }
    }
}

fun UpdateOrderRequest.validate() = apply {
    if (orderItems.isEmpty()) throw InsufficientAmountOfOrderItemException()
    orderItems.forEach {
        if (it.productNumber < 0 || it.quantity <= 0) {
            throw OrderRequestException("Not correct product number or quantity")
        }
    }
}

fun Long.validate(functor: (Long) -> Optional<Order>): Order =
    run {
        functor(this).orElseThrow { OrderNotFoundException(this) }
    }
