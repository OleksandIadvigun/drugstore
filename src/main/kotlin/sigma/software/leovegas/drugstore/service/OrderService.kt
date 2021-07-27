package sigma.software.leovegas.drugstore.service

import java.math.BigDecimal
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import sigma.software.leovegas.drugstore.dto.OrderRequest
import sigma.software.leovegas.drugstore.dto.OrderResponse
import sigma.software.leovegas.drugstore.exception.InsufficientAmountOfProductForOrderException
import sigma.software.leovegas.drugstore.exception.OrderNotFoundException
import sigma.software.leovegas.drugstore.persistence.entity.OrderDetails
import sigma.software.leovegas.drugstore.persistence.entity.Order
import sigma.software.leovegas.drugstore.persistence.repository.OrderRepository
import sigma.software.leovegas.drugstore.persistence.repository.ProductRepository
import sigma.software.leovegas.drugstore.toEntity
import sigma.software.leovegas.drugstore.toOrderResponse
import sigma.software.leovegas.drugstore.toOrderResponseList

@Service
@Transactional
class OrderService @Autowired constructor(
    private val orderRepository: OrderRepository,
    private val productRepository: ProductRepository
) {

    fun getOrderById(id: Long): OrderResponse =
        orderRepository.findById(id).orElseThrow { OrderNotFoundException(id) }.toOrderResponse()


    fun getAllOrders(): List<OrderResponse> {
        val orderList = orderRepository.findAll()
        return orderList.toOrderResponseList()
    }

    fun postOrder(orderRequest: OrderRequest): OrderResponse {
        if (orderRequest.orderDetailsList.isEmpty()) {
            throw InsufficientAmountOfProductForOrderException()
        }
        val orderDetailsList = makeFullOrderDetailList(orderRequest)
        val orderToSave = Order(
            orderDetailsList = orderDetailsList,
            total = calculateTotal(orderDetailsList)
        )
        val savedOrder = orderRepository.save(orderToSave).toOrderResponse()
        return savedOrder
    }


    fun updateOrder(id: Long, orderRequest: OrderRequest): OrderResponse {
        if (orderRequest.orderDetailsList.isEmpty()) {
            throw InsufficientAmountOfProductForOrderException()
        }
        val orderToChange = getOrderById(id).toEntity()
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

    fun cancelOrder(id: Long) {
        val orderToDelete = getOrderById(id).toEntity()
        orderRepository.delete(orderToDelete)
    }
}




