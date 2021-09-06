package sigma.software.leovegas.drugstore.order

import java.math.BigDecimal
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import sigma.software.leovegas.drugstore.accountancy.api.CreateOutcomeInvoiceRequest
import sigma.software.leovegas.drugstore.accountancy.api.InvoiceResponse
import sigma.software.leovegas.drugstore.accountancy.api.ItemDTO
import sigma.software.leovegas.drugstore.accountancy.client.AccountancyClient
import sigma.software.leovegas.drugstore.order.OrderStatus.CREATED
import sigma.software.leovegas.drugstore.order.OrderStatus.PAID
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
    val accountancyClient: AccountancyClient,
) {

    fun createOrder(createOrderRequest: CreateOrderRequest): OrderResponse = createOrderRequest.run {
        if (orderItems.isEmpty()) throw InsufficientAmountOfOrderItemException()
        val entity = toEntity().copy(orderStatus = CREATED) // NOTE: business logic must be placed in services!
        val created = orderRepository.save(entity)
        created.toOrderResponseDTO()
    }

    fun getOrderById(id: Long): OrderResponse =
        orderRepository.findById(id).orElseThrow { throw OrderNotFoundException(id) }.toOrderResponseDTO()

    fun getOrdersByStatus(orderStatus: OrderStatusDTO): List<OrderResponse> =
        orderRepository.getAllByOrderStatus(OrderStatus.valueOf(orderStatus.name)).toOrderResponseList()

    fun getOrders(): List<OrderResponse> {
        val orderList = orderRepository.findAll()
        return orderList.toOrderResponseList()
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

    fun changeOrderStatus(id: Long, orderStatus: OrderStatusDTO) =
        id.run {
            val toUpdate = orderRepository
                .findById(this)
                .orElseThrow { OrderNotFoundException(id) }
                .copy(orderStatus = OrderStatus.valueOf(orderStatus.name))
            val updated = orderRepository.saveAndFlush(toUpdate)
            updated.toOrderResponseDTO()
        }

    fun getOrderDetails(id: Long) = id.run {
        val orderById = getOrderById(this)
        if (orderById.orderStatus == OrderStatusDTO.NONE) {
            throw OrderNotCreatedException(orderById.id)
        }
        val orderItemsQuantity = orderById.orderItems.associate { it.productId to it.quantity }
        val orderItemsIds = orderById.orderItems.map { it.productId }
        val products = productClient.getProductsDetailsByIds(orderItemsIds)
        val orderItemDetails = products.map {
            OrderItemDetailsDTO(
                productId = it.id,
                name = it.name,
                quantity = orderItemsQuantity[it.id] ?: -1,
                price = it.price
            )
        }
        OrderDetailsDTO(
            orderItemDetails = orderItemDetails,
            total = orderItemDetails.map { it.price.multiply(BigDecimal(it.quantity)) }.reduce(BigDecimal::plus)
                .setScale(2)
        )
    }

    fun confirmOrder(orderId: Long): InvoiceResponse {
        val order = getOrderById(orderId)
        if (order.orderItems.isEmpty()) throw InsufficientAmountOfOrderItemException()
        if ((order.orderStatus != OrderStatusDTO.CREATED).and(order.orderStatus != OrderStatusDTO.UPDATED)) {
            throw OrderStatusException("Order is already confirmed or cancelled")
        }
        try {
            val invoice = accountancyClient.createOutcomeInvoice(
                CreateOutcomeInvoiceRequest(
                    order.orderItems.map { ItemDTO(productId = it.productId, quantity = it.quantity) }, orderId
                )
            )
            changeOrderStatus(orderId, OrderStatusDTO.CONFIRMED)
            return invoice
        } catch (e: Exception) {
            throw AccountancyServerNotAvailable()
        }
    }

    fun getProductsIdToQuantity(): Map<Long, Int> {
        val ids = orderRepository
            .getAllByOrderStatus(PAID)
            .map { it.orderItems.map { item -> item.id ?: -1 } }
            .flatten()
        return orderRepository.getIdToQuantity(ids).associate { it.priceItemId to it.quantity }
    }
}
