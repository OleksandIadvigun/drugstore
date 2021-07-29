package sigma.software.leovegas.drugstore.order

import java.math.BigDecimal
import java.util.Optional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import sigma.software.leovegas.drugstore.product.ProductRepository

@Service
@Transactional
class OrderService @Autowired constructor(
    private val orderRepository: OrderRepository,
    private val productRepository: ProductRepository
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

    fun getInvoice(id: Long): OrderInvoice {
        val order = getOrderById(id)
        val productMap = order.orderItems.associate { it.productId to it.quantity }
        val ids = productMap.keys
        val total = productRepository.findProductsView(ids).map {
            it.price.multiply(
                BigDecimal(productMap.getValue(it.productId))
            )
        }
            .reduce(BigDecimal::plus)
            .setScale(2)
        return OrderInvoice(order.id, total)
    }
}
