package sigma.software.leovegas.drugstore.order

import java.math.BigDecimal
import java.math.RoundingMode
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import sigma.software.leovegas.drugstore.accountancy.api.ConfirmOrderResponse
import sigma.software.leovegas.drugstore.accountancy.api.CreateOutcomeInvoiceRequest
import sigma.software.leovegas.drugstore.accountancy.api.ItemDTO
import sigma.software.leovegas.drugstore.accountancy.client.AccountancyClient
import sigma.software.leovegas.drugstore.api.messageSpliterator
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

    fun createOrder(createOrderRequest: CreateOrderRequest): OrderResponse =
        createOrderRequest.validate().run {
            val entity = toEntity().copy(orderStatus = CREATED) // NOTE: business logic must be placed in services!
            val created = orderRepository.save(entity).toOrderResponseDTO()
            logger.info("Order created $created")
            return@run created
        }

    fun getOrderById(id: Long): OrderResponse = id.validate(orderRepository::findById).run {
        logger.info("Order found $this")
        return@run this.toOrderResponseDTO()
    }

    fun getOrdersByStatus(orderStatus: OrderStatusDTO): List<OrderResponse> = orderStatus.run {
        val orders = orderRepository.getAllByOrderStatus(OrderStatus.valueOf(this.name)).toOrderResponseList()
        logger.info("Orders found by status $orders")
        return@run orders
    }

    fun getOrders(): List<OrderResponse> {
        val orders = orderRepository.findAll()
        logger.info("Orders found $orders")
        return orders.toOrderResponseList()
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
            logger.info("Order updated $updated")
            updated.toOrderResponseDTO()
        }

    fun changeOrderStatus(id: Long, orderStatus: OrderStatusDTO) =
        id.validate(orderRepository::findById).run {
            val orderToUpdate = this.copy(orderStatus = OrderStatus.valueOf(orderStatus.name))
            val updatedOrder = orderRepository.saveAndFlush(orderToUpdate)
            logger.info("Order with changed status $updatedOrder")
            return@run updatedOrder.toOrderResponseDTO()
        }

    fun getOrderDetails(id: Long) =
        id.validate(orderRepository::findById).run {
            if (this.orderStatus == OrderStatus.NONE) throw OrderNotCreatedException(id)

            val orderItemsQuantity = orderItems.associate { it.productId to it.quantity }
            val orderItemsIds = orderItems.map { it.productId }
            val products = runCatching {
                productClient.getProductsDetailsByIds(orderItemsIds)
            }
                .onFailure { error -> throw ProductServerException(error.localizedMessage.messageSpliterator()) }
                .getOrThrow()
            logger.info("Received products details $products")

            val price = runCatching {
                accountancyClient.getSalePrice(orderItemsIds)
            }
                .onFailure { error -> throw AccountancyServerException(error.localizedMessage.messageSpliterator()) }
                .getOrThrow()
            logger.info("Received products prices $price")

            val orderItemDetails = products.map {
                OrderItemDetailsDTO(
                    productNumber = it.productNumber,
                    name = it.name,
                    quantity = orderItemsQuantity[it.productNumber] ?: -1,
                    price = price.getValue(it.productNumber).setScale(2, RoundingMode.HALF_EVEN)
                )
            }

            val orderDetails = OrderDetailsDTO(
                orderItemDetails = orderItemDetails,
                total = orderItemDetails.map { it.price.multiply(BigDecimal(it.quantity)) }.reduce(BigDecimal::plus)

            )
            logger.info("Return orderDetails $orderDetails")
            return@run orderDetails
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
                    .onFailure { error -> throw AccountancyServerException(error.localizedMessage.messageSpliterator()) }
                    .getOrThrow()
            }

    fun getProductsIdToQuantity(): Map<Long, Int> {
        val ids = orderRepository
            .getAllByOrderStatus(CONFIRMED)
            .map { it.orderItems.map { item -> item.id ?: -1 } }
            .flatten()
        logger.info("Sorted list of ids by most buys $ids")
        return orderRepository.getIdToQuantity(ids).associate { it.priceItemId to it.quantity }
    }
}
