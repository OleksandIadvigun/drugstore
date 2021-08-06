package sigma.software.leovegas.drugstore.order

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import sigma.software.leovegas.drugstore.order.OrderStatus.CREATED
import sigma.software.leovegas.drugstore.order.OrderStatus.UPDATED
import sigma.software.leovegas.drugstore.order.api.CreateOrderRequest
import sigma.software.leovegas.drugstore.order.api.OrderResponse
import sigma.software.leovegas.drugstore.order.api.UpdateOrderRequest

@Service
@Transactional
class OrderService(private val orderRepository: OrderRepository) {

    fun getOrderById(id: Long): OrderResponse =
        orderRepository.findById(id).orElseThrow { throw OrderNotFoundException(id) }.toOrderResponseDTO()

    fun getOrders(): List<OrderResponse> {
        val orderList = orderRepository.findAll()
        return orderList.toOrderResponseList()
    }

    fun createOrder(createOrderRequest: CreateOrderRequest): OrderResponse = createOrderRequest.run {
        if (orderItems.isEmpty()) throw InsufficientAmountOfOrderItemException()
        val entity = toEntity().copy(orderStatus = CREATED) // NOTE: business logic must be placed in services!
        val created = orderRepository.save(entity)
        created.toOrderResponseDTO()
    }

    fun updateOrder(id: Long, updateOrderRequest: UpdateOrderRequest): OrderResponse =
        updateOrderRequest.run {
            if (orderItems.isEmpty()) throw InsufficientAmountOfOrderItemException()
            val toUpdate = orderRepository
                .findById(id)
                .orElseThrow { OrderNotFoundException(id) }
                .copy(
                    orderStatus = UPDATED, // NOTE: business logic must be placed in services!
                    orderItems = orderItems.toEntities(),
                )
            val updated = orderRepository.save(toUpdate)
            updated.toOrderResponseDTO()
        }

    fun deleteOrder(id: Long) {
        val orderToDelete = getOrderById(id).toEntity()
        orderRepository.delete(orderToDelete)
    }
}
