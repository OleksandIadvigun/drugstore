package sigma.software.leovegas.drugstore.order

import java.math.BigDecimal
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import sigma.software.leovegas.drugstore.accountancy.api.ConfirmOrderResponse
import sigma.software.leovegas.drugstore.accountancy.api.CreateOutcomeInvoiceRequest
import sigma.software.leovegas.drugstore.accountancy.api.ItemDTO
import sigma.software.leovegas.drugstore.accountancy.client.AccountancyClient
import sigma.software.leovegas.drugstore.order.OrderStatus.CONFIRMED
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
    val accountancyClient: AccountancyClient,
) {

    val logger: Logger = LoggerFactory.getLogger(OrderService::class.java)

    fun createOrder(createOrderRequest: CreateOrderRequest): OrderResponse = createOrderRequest.validate().run {
        val entity = toEntity().copy(orderStatus = CREATED) // NOTE: business logic must be placed in services!
        val created = orderRepository.save(entity)
        created.toOrderResponseDTO()
    }

    fun getOrderById(id: Long): OrderResponse =
        id.validate(orderRepository::findById).toOrderResponseDTO()

    fun getOrdersByStatus(orderStatus: OrderStatusDTO): List<OrderResponse> =
        orderRepository.getAllByOrderStatus(OrderStatus.valueOf(orderStatus.name)).toOrderResponseList()

    fun getOrders(): List<OrderResponse> {
        val orderList = orderRepository.findAll()
        return orderList.toOrderResponseList()
    }

    fun updateOrder(id: Long, updateOrderRequest: UpdateOrderRequest): OrderResponse =
        updateOrderRequest.validate().run {
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
            val toUpdate =
                validate(orderRepository::findById)
                    .copy(orderStatus = OrderStatus.valueOf(orderStatus.name))
            val updated = orderRepository.saveAndFlush(toUpdate)
            updated.toOrderResponseDTO()
        }

    fun getOrderDetails(id: Long) =
        id.validate(orderRepository::findById).run {
            if (this.orderStatus == OrderStatus.NONE) {
                throw OrderNotCreatedException(id)
            }
            val orderItemsQuantity = orderItems.associate { it.productId to it.quantity }
            val orderItemsIds = orderItems.map { it.productId }
            val products = productClient.getProductsDetailsByIds(orderItemsIds)
            logger.info("Received products details $products")
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

    fun confirmOrder(orderId: Long): ConfirmOrderResponse =
        orderId.validate(orderRepository::findById)
            .run {
                if (orderItems.isEmpty()) throw InsufficientAmountOfOrderItemException()
                if ((orderStatus != CREATED).and(orderStatus != UPDATED)) {
                    throw OrderStatusException("Order is already confirmed or cancelled")
                }
                runCatching {
                    val createOutcomeInvoice = accountancyClient.createOutcomeInvoice(
                        CreateOutcomeInvoiceRequest(
                            orderItems.map {
                                ItemDTO(productId = it.productId, quantity = it.quantity)
                            }, orderId
                        )
                    )
                    changeOrderStatus(orderId, OrderStatusDTO.CONFIRMED)
                    logger.info("Invoice was created $createOutcomeInvoice")
                    createOutcomeInvoice
                }
                    .onFailure { throw AccountancyServerNotAvailable() }
                    .getOrThrow()
            }

    fun getProductsIdToQuantity(): Map<Long, Int> {
        val ids = orderRepository
            .getAllByOrderStatus(CONFIRMED)
            .map { it.orderItems.map { item -> item.id ?: -1 } }
            .flatten()
        return orderRepository.getIdToQuantity(ids).associate { it.priceItemId to it.quantity }
    }
}
