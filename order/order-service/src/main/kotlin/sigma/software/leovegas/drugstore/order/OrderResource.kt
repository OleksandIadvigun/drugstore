package sigma.software.leovegas.drugstore.order

import java.util.UUID
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.stream.function.StreamBridge
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.support.MessageBuilder
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import sigma.software.leovegas.drugstore.api.ApiError
import sigma.software.leovegas.drugstore.api.protobuf.Proto
import sigma.software.leovegas.drugstore.order.api.CreateOrderEvent
import sigma.software.leovegas.drugstore.order.api.OrderStatusDTO
import sigma.software.leovegas.drugstore.order.api.UpdateOrderEvent

@CrossOrigin
@RestController
@RequestMapping("/api/v1/orders")
class OrderResource @Autowired constructor(
    val orderService: OrderService,
    val eventStream: StreamBridge,
) {

    val logger: Logger = LoggerFactory.getLogger(OrderResource::class.java)

    @PostMapping(path = ["", "/"])
    @ResponseStatus(HttpStatus.CREATED)
    fun createOrder(@RequestBody createOrderEvent: CreateOrderEvent): String {
        val generatedOrderNumber = UUID.randomUUID().toString()
        val createOrderMessage = MessageBuilder.createMessage(
            createOrderEvent.copy(orderNumber = generatedOrderNumber),
            MessageHeaders(mutableMapOf(Pair<String, Any>("createOrder", true)))
        )
        eventStream.send("createUpdateOrderEventPublisher-out-0", createOrderMessage)
        return generatedOrderNumber;
    }

    @GetMapping("/{orderNumber}")
    @ResponseStatus(HttpStatus.OK)
    fun getOrderById(@PathVariable("orderNumber") orderNumber: String) =
        orderService.getOrderByOrderNumber(orderNumber)

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/status/{status}")
    fun getOrdersByStatus(@PathVariable("status") orderStatus: OrderStatusDTO) =
        orderService.getOrdersByStatus(orderStatus)

    @GetMapping("/total-buys")
    @ResponseStatus(HttpStatus.OK)
    fun getProductsIdToQuantity(): Proto.ProductQuantityMap = orderService.getProductsNumberToQuantity()

    @GetMapping("/{orderNumber}/details")
    @ResponseStatus(HttpStatus.OK)
    fun getOrderDetails(@PathVariable("orderNumber") orderNumber: String) = orderService.getOrderDetails(orderNumber)

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = ["", "/"])
    fun getOrders(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "5") size: Int,
    ) = orderService.getOrders(page, size)

    @PutMapping("/{orderNumber}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun updateOrder(
        @PathVariable("orderNumber") orderNumber: String,
        @RequestBody updateOrderEvent: UpdateOrderEvent
    ): String {
        val updateOrderMessage = MessageBuilder.createMessage(
            updateOrderEvent.copy(orderNumber = orderNumber),
            MessageHeaders(mutableMapOf(Pair<String, Any>("createOrder", false)))
        )
        eventStream.send("createUpdateOrderEventPublisher-out-0", updateOrderMessage)
        return "Updated"
    }

    @PostMapping("/confirm/{orderNumber}")
    @ResponseStatus(HttpStatus.CREATED)
    fun confirmOrder(@PathVariable("orderNumber") orderNumber: String) =
        orderService.confirmOrder(orderNumber)

    @ExceptionHandler(Throwable::class)
    fun handleNotFound(e: Throwable) = run {
        val status = when (e) {
            is AccountancyServerException -> HttpStatus.GATEWAY_TIMEOUT
            is ProductServerException -> HttpStatus.GATEWAY_TIMEOUT
            else -> HttpStatus.BAD_REQUEST
        }
        val error = ApiError(status.value(), status.reasonPhrase, e.message)
        logger.warn("$error , ${e.javaClass}")
        ResponseEntity.status(status).body(error)
    }
}
