package sigma.software.leovegas.drugstore.order

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class OrderService @Autowired constructor(
    private val orderRepository: OrderRepository,
) {

    fun getOrderById(id: Long): CreateOrderResponse =
        orderRepository.findById(id).orElseThrow { throw OrderNotFoundException(id) }.toCreateOrderResponse()

    fun getOrders(): List<CreateOrderResponse> {
        val orderList = orderRepository.findAll()
        return orderList.toOrderResponseList()
    }

    fun createOrder(createOrderRequest: CreateOrderRequest): CreateOrderResponse = createOrderRequest.run {
        if (this.orderItems.isEmpty()) throw InsufficientAmountOfOrderItemException()
        orderRepository.save(this.toOrder()).toCreateOrderResponse()
    }

    fun updateOrder(id: Long, updateOrderRequest: UpdateOrderRequest): UpdateOrderResponse {
        if (!orderRepository.findById(id).isPresent) throw OrderNotFoundException(id)
        if (updateOrderRequest.orderItems.isEmpty()) throw InsufficientAmountOfOrderItemException()
        val changedOrder = orderRepository.save(
            Order(
                id = id,
                orderStatus = updateOrderRequest.toOrder().orderStatus,
                orderItems = updateOrderRequest.toOrder().orderItems
            )
        )
        return changedOrder.toUpdateOrderResponse()

    }

    fun deleteOrder(id: Long) {
        val orderToDelete = getOrderById(id).toEntity()
        orderRepository.delete(orderToDelete)
    }
}
