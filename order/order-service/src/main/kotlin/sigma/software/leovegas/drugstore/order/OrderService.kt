package sigma.software.leovegas.drugstore.order

import java.math.BigDecimal
import java.math.RoundingMode
import java.util.UUID
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
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
            val entity = toEntity().copy(
                orderStatus = CREATED,
                orderNumber = UUID.randomUUID().toString()
            ) // NOTE: business logic must be placed in services!
            val created = orderRepository.save(entity).toOrderResponseDTO()
            logger.info("Order created $created")
            return@run created
        }

    fun getOrderByOrderNumber(orderNumber: String): OrderResponse =
        orderNumber.validate(orderRepository::findByOrderNumber).run {
        logger.info("Order found $this")
        return@run this.toOrderResponseDTO()
    }

    fun getOrdersByStatus(orderStatus: OrderStatusDTO): List<OrderResponse> =
        orderStatus.run {
        val orders = orderRepository.getAllByOrderStatus(OrderStatus.valueOf(this.name)).toOrderResponseList()
        logger.info("Orders found by status $orders")
        return@run orders
    }

    fun getOrders(page: Int, size: Int): List<OrderResponse> {
        val pageable: Pageable = PageRequest.of(page, size)
        val orders = orderRepository.findAll(pageable).content
        logger.info("Orders found $orders")
        return orders.toOrderResponseList()
    }

    fun updateOrder(orderNumber: String, updateOrderRequest: UpdateOrderRequest): OrderResponse =
        updateOrderRequest.validate().run {
            val toUpdate = orderRepository
                .findByOrderNumber(orderNumber)
                .orElseThrow { OrderNotFoundException(orderNumber) }
                .copy(
                    orderStatus = UPDATED, // NOTE: business logic must be placed in services!
                    orderItems = orderItems.toEntities(),
                )
            val updated = orderRepository.saveAndFlush(toUpdate)
            logger.info("Order updated $updated")
            updated.toOrderResponseDTO()
        }

    fun changeOrderStatus(orderNumber: String, orderStatus: OrderStatusDTO) =
        orderNumber.validate(orderRepository::findByOrderNumber).run {
            val orderToUpdate = this.copy(orderStatus = OrderStatus.valueOf(orderStatus.name))
            val updatedOrder = orderRepository.saveAndFlush(orderToUpdate)
            logger.info("Order with changed status $updatedOrder")
            return@run updatedOrder.toOrderResponseDTO()
        }

    fun getOrderDetails(orderNumber: String) =
        orderNumber.validate(orderRepository::findByOrderNumber).run {
            if (this.orderStatus == OrderStatus.NONE) throw OrderNotCreatedException(orderNumber)
            val orderItemsQuantity = orderItems.associate { it.productNumber to it.quantity }
            val orderItemsIds = orderItems.map { it.productNumber }
            val products = runCatching {
                productClient.getProductsDetailsByProductNumbers(orderItemsIds)
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
                orderNumber = orderNumber,
                orderItemDetails = orderItemDetails,
                total = orderItemDetails.map { it.price.multiply(BigDecimal(it.quantity)) }.reduce(BigDecimal::plus)

            )
            logger.info("Return orderDetails $orderDetails")
            return@run orderDetails
        }

    fun confirmOrder(orderNumber: String): ConfirmOrderResponse =
        orderNumber.validate(orderRepository::findByOrderNumber)
            .run {
                if (orderItems.isEmpty()) throw InsufficientAmountOfOrderItemException()
                if ((orderStatus != CREATED).and(orderStatus != UPDATED)) {
                    throw OrderStatusException("Order is already confirmed or cancelled")
                }
                runCatching {
                    val createOutcomeInvoice = accountancyClient.createOutcomeInvoice(
                        CreateOutcomeInvoiceRequest(
                            orderItems.map {
                                ItemDTO(productNumber = it.productNumber, quantity = it.quantity)
                            },
                            orderNumber
                        )
                    )
                    changeOrderStatus(orderNumber, OrderStatusDTO.CONFIRMED)
                    logger.info("Invoice was created $createOutcomeInvoice")
                    createOutcomeInvoice
                }
                    .onFailure { error -> throw AccountancyServerException(error.localizedMessage.messageSpliterator()) }
                    .getOrThrow()
            }

    fun getProductsNumberToQuantity(): Map<String, Int> {
        val productNumbers = orderRepository
            .getAllByOrderStatus(CONFIRMED)
            .map { it.orderItems.map { item -> item.productNumber } }
            .flatten()
        logger.info("Sorted list of ids by most buys $productNumbers")
        return orderRepository.getProductNumberToQuantity(productNumbers).associate { it.productNumber to it.quantity }
    }
}
