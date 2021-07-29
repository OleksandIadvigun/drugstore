package sigma.software.leovegas.drugstore.order

import java.math.BigDecimal
import java.util.*
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
                    orderDetailsList = order.orderDetailsList
                        .map {
                            OrderDetailsResponse(
                                it.product.id,
                                it.product.name,
                                it.product.price,
                                it.quantity
                            )
                        }
                )
            }

    fun getOrders(): List<OrderResponse> {
        val orderList = orderRepository.findAll()
        return orderList.toOrderResponseList()
    }

    fun createOrder(orderRequest: OrderRequest): OrderResponse =
        orderRequest.orderDetailsList.run {
            if (this.isEmpty()) throw InsufficientAmountOfProductForOrderException()
            val orderToSave = Order(
                orderDetailsList = makeFullOrderDetailList(orderRequest)
            )
            orderRepository.save(orderToSave).toOrderResponse()
        }

    fun updateOrder(id: Long, orderRequest: OrderRequest): OrderResponse {
        val orderToChange = getOrderById(id).toEntity()
        if (orderRequest.orderDetailsList.isEmpty()) {
            throw InsufficientAmountOfProductForOrderException()
        }
        orderToChange.orderDetailsList = makeFullOrderDetailList(orderRequest)
        val changedOrder = orderRepository.save(orderToChange)
        return changedOrder.toOrderResponse()

    }

    fun deleteOrder(id: Long) {
        val orderToDelete = getOrderById(id).toEntity()
        orderRepository.delete(orderToDelete)
    }

    fun getInvoice(id: Long): OrderInvoice {
        val order = getOrderById(id)
        return OrderInvoice(
            order.id,
            order.orderDetailsList
                .map { it.price.multiply(BigDecimal(it.quantity)) }
                .reduce(BigDecimal::plus).setScale(2)
        )

    }

    private fun makeFullOrderDetailList(orderRequest: OrderRequest): MutableList<OrderDetails> {
        val requestMap = orderRequest.orderDetailsList.associate { it.productId to it.quantity }
        val orderDetailsList = mutableListOf<OrderDetails>()
        var i = 0
        val products = productRepository.findAllById(orderRequest.orderDetailsList.map { it.productId })
        for (orderDetails in orderRequest.orderDetailsList) {
            orderDetailsList.add(
                OrderDetails(
                    product = products[i],
                    quantity = requestMap[products[i].id]!!
                )
            )
            i++
        }
        return orderDetailsList
    }


}
