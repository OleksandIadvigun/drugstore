package sigma.software.leovegas.drugstore.order

import java.util.Optional
import sigma.software.leovegas.drugstore.order.api.CreateOrderEvent
import sigma.software.leovegas.drugstore.order.api.UpdateOrderEvent

fun CreateOrderEvent.validate() = apply {
    if (orderItems.isEmpty()) throw InsufficientAmountOfOrderItemException()
    orderItems.forEach {
        if (it.productNumber.isBlank() || it.quantity <= 0) {
            throw OrderRequestException("Not correct product number or quantity")
        }
    }
}

fun UpdateOrderEvent.validate() = apply {
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
