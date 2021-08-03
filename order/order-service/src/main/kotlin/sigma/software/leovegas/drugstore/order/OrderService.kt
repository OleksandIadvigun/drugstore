package sigma.software.leovegas.drugstore.order


import java.util.Optional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class OrderService @Autowired constructor(
    private val orderRepository: OrderRepository,
) {

    fun getOrderById(id: Long): OrderResponse =
        Optional.ofNullable(id).orElseThrow { OrderNotFoundException(id) }
            .run {
                val order = orderRepository.findById(this)
                    .orElseThrow { OrderNotFoundException(id) }
                OrderResponse(
                    id = order.id,
                    orderItems = order.orderItems
                        .map {
                            OrderItem(
                                id = id,
                                productId = it.productId,
                                quantity = it.quantity
                            )
                        }.toSet()
                )
            }

    fun getOrders(): List<OrderResponse> {
        val orderList = orderRepository.findAll()
        return orderList.toOrderResponseList()
    }

    fun createOrder(orderRequest: OrderRequest): OrderResponse = orderRequest.run {
        if (this.orderItems.isEmpty()) throw InsufficientAmountOfOrderItemException()
        orderRepository.save(this.toOrder()).toOrderResponse()
    }

    fun updateOrder(id: Long, orderRequest: OrderRequest): OrderResponse {
        if (!orderRepository.findById(id).isPresent) throw OrderNotFoundException(id)
        if (orderRequest.orderItems.isEmpty()) throw InsufficientAmountOfOrderItemException()
        val changedOrder = orderRepository.save(Order(id, orderRequest.orderItems))
        return changedOrder.toOrderResponse()

    }

    fun deleteOrder(id: Long) {
        val orderToDelete = getOrderById(id).toEntity()
        orderRepository.delete(orderToDelete)
    }
}
