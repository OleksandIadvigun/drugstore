package sigma.software.leovegas.drugstore.order

import java.math.BigDecimal
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import sigma.software.leovegas.drugstore.accountancy.client.AccountancyClient
import sigma.software.leovegas.drugstore.order.OrderStatus.CREATED
import sigma.software.leovegas.drugstore.order.OrderStatus.UPDATED
import sigma.software.leovegas.drugstore.order.api.CreateOrderRequest
import sigma.software.leovegas.drugstore.order.api.OrderDetailsDTO
import sigma.software.leovegas.drugstore.order.api.OrderItemDetailsDTO
import sigma.software.leovegas.drugstore.order.api.OrderResponse
import sigma.software.leovegas.drugstore.order.api.OrderStatusDTO
import sigma.software.leovegas.drugstore.order.api.UpdateOrderRequest
import sigma.software.leovegas.drugstore.product.client.ProductClient

@Service
@Transactional
class OrderService @Autowired constructor(
    val orderRepository: OrderRepository,
    val productClient: ProductClient,
    val accountancyClient: AccountancyClient
) {

    fun getOrderById(id: Long): OrderResponse =
        orderRepository.findById(id).orElseThrow { throw OrderNotFoundException(id) }.toOrderResponseDTO()

    fun getOrdersByStatus(orderStatus: OrderStatusDTO): List<OrderResponse> =
        orderRepository.getAllByOrderStatus(OrderStatus.valueOf(orderStatus.name)).toOrderResponseList()

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

    fun getOrderDetails(id: Long): OrderDetailsDTO = id.run {
        val orderById = getOrderById(this)
        val priceItemIds = orderById.orderItems.map { it.priceItemId }
        val priceItems = accountancyClient.getPriceItemsByIds(priceItemIds)
        val productMap = priceItems.associate { it.id to it.productId }
        val priceMap = priceItems.associate { it.id to it.price }
        val nameMap = productClient.getProductsByIds(productMap.values.toList()).associate { it.id to it.name }
        var orderItemDetails: List<OrderItemDetailsDTO> =
            orderById.orderItems.map {
                OrderItemDetailsDTO(
                    priceItemId = it.priceItemId,
                    name = nameMap[productMap[it.priceItemId]] ?: "undefined",
                    quantity = it.quantity,
                    price = priceMap[it.priceItemId] ?: BigDecimal("-1")
                )
            }
        OrderDetailsDTO(
            orderItemDetails = orderItemDetails,
            total = orderItemDetails.map { it.price.multiply(BigDecimal(it.quantity)) }.reduce(BigDecimal::plus)
                .setScale(2)
        )
    }

    fun getProductsIdToQuantity(): Map<Long, Int> {
        return orderRepository.getIdToQuantity().associate { it.priceItemId to it.quantity }
    }

    fun changeOrderStatus(id: Long, orderStatus: OrderStatusDTO) =
        id.run {
            val toUpdate = orderRepository
                .findById(this)
                .orElseThrow { OrderNotFoundException(id) }
                .copy(orderStatus = OrderStatus.valueOf(orderStatus.name))
            val updated = orderRepository.saveAndFlush(toUpdate)
            updated.toOrderResponseDTO()
        }
}
