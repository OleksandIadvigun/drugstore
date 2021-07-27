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

    fun getOrderById(id: Long?): OrderResponse =
        Optional.ofNullable(id).orElseThrow { OrderNotFoundException(id) }
            .run {
                val order = orderRepository.findById(this)
                    .orElseThrow { OrderNotFoundException(id) }
                OrderResponse(
                    id=order.id,
                    total =order.total ?: BigDecimal.ZERO,
                    orderDetailsList= order.orderDetailsList
                        .orEmpty()
                        .map {
                            OrderDetailsResponse(
                                it.product?.id ?: -1,
                                it.product?.name ?: "invalid",
                                it.product?.price ?: BigDecimal.ZERO,
                                it.quantity ?: -1
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
            val orderDetailsList = makeFullOrderDetailList(orderRequest)
            val orderToSave = Order(
                orderDetailsList = orderDetailsList,
                total = calculateTotal(orderDetailsList)
            )
            orderRepository.save(orderToSave).toOrderResponse()
        }

    fun updateOrder(id: Long?, orderRequest: OrderRequest): OrderResponse {
        val orderToChange = getOrderById(id).toEntity()
        if (orderRequest.orderDetailsList.isEmpty()) {
            throw InsufficientAmountOfProductForOrderException()
        }
        val orderDetailsList = makeFullOrderDetailList(orderRequest)

        orderToChange.orderDetailsList = orderDetailsList
        orderToChange.total = calculateTotal(orderDetailsList)
        val changedOrder = orderRepository.save(orderToChange)
        return changedOrder.toOrderResponse()

    }

    private fun calculateTotal(orderDetailsList: List<OrderDetails>): BigDecimal {
        return orderDetailsList.map {
            it.product?.price!!
                .multiply(it.quantity?.toBigDecimal())
        }.reduce { total, productValue -> total.add(productValue) }
    }

    private fun makeFullOrderDetailList(orderRequest: OrderRequest): MutableList<OrderDetails> {
        val requestMap = orderRequest.orderDetailsList.associate { it.productId to it.quantity }
        val orderDetailsList = mutableListOf<OrderDetails>()
        var i: Int = 0
        val products = productRepository.findAllById(orderRequest.orderDetailsList.map { it.productId })
        for (orderDetails in orderRequest.orderDetailsList) {
            orderDetailsList.add(
                OrderDetails(
                    product = products[i],
                    quantity = requestMap[products[i].id]
                )
            )
            i++
        }
        return orderDetailsList
    }

    fun deleteOrder(id: Long?) {
        val orderToDelete = getOrderById(id).toEntity()
        orderRepository.delete(orderToDelete)
    }
}
