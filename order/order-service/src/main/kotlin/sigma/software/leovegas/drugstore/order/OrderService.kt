package sigma.software.leovegas.drugstore.order

import java.math.BigDecimal
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import sigma.software.leovegas.drugstore.order.OrderStatus.CREATED
import sigma.software.leovegas.drugstore.order.OrderStatus.UPDATED
import sigma.software.leovegas.drugstore.order.api.CreateOrderRequest
import sigma.software.leovegas.drugstore.order.api.OrderDetailsDTO
import sigma.software.leovegas.drugstore.order.api.OrderItemDetailsDTO
import sigma.software.leovegas.drugstore.order.api.OrderResponse
import sigma.software.leovegas.drugstore.order.api.UpdateOrderRequest
import sigma.software.leovegas.drugstore.product.client.ProductClient

@Service
@Transactional
class OrderService @Autowired constructor(
    val orderRepository: OrderRepository,
    val productClient: ProductClient,
) {

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
            val updated = orderRepository.saveAndFlush(toUpdate)
            updated.toOrderResponseDTO()
        }

    fun deleteOrder(id: Long) {
        val orderToDelete = getOrderById(id).toEntity()
        orderRepository.delete(orderToDelete)
    }

    fun getOrderDetails(id: Long): OrderDetailsDTO = id.run {
        val orderById = getOrderById(this)
        val productIdList = orderById.orderItems.map { it.productId }
        val products = productClient.getProductsByIds(productIdList).associateBy { it.id }
        val orderItemDetails = orderById.orderItems.map {
            OrderItemDetailsDTO(
                name = products[it.productId]?.name ?: "undefined",
                price = products[it.productId]?.price ?: BigDecimal("-1"),
                quantity = it.quantity
            )
        }
        OrderDetailsDTO(
            orderItemDetails = orderItemDetails,
            total = orderItemDetails.map { it.price.multiply(BigDecimal(it.quantity)) }.reduce(BigDecimal::plus)
                .setScale(2)
        )
    }

    fun getProductsIdToQuantity(): Map<Long, Int> {
        val map = mutableMapOf<Long, Int>()
        orderRepository.findAll().forEach { o ->
            o.orderItems.forEach { i ->
                if (map.keys.contains(i.productId)) {
                    val prevQuantity = map[i.productId]
                    val newQuantity = i.quantity + (prevQuantity ?: -1)
                    map[i.productId] = newQuantity
                } else {
                    map[i.productId] = i.quantity
                }
            }
        }
        return map.toList().sortedByDescending { (_, value) -> value }.toMap()
    }
}
