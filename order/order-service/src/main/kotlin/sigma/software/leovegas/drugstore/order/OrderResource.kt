package sigma.software.leovegas.drugstore.order

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
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
import sigma.software.leovegas.drugstore.order.api.CreateOrderRequest
import sigma.software.leovegas.drugstore.order.api.OrderStatusDTO
import sigma.software.leovegas.drugstore.order.api.UpdateOrderRequest

@RestController
@RequestMapping("/api/v1/orders")
class OrderResource(private val orderService: OrderService) {

    val logger: Logger = LoggerFactory.getLogger(OrderResource::class.java)

    @PostMapping(path = ["", "/"])
    @ResponseStatus(HttpStatus.CREATED)
    fun createOrder(@RequestBody createOrderRequest: CreateOrderRequest) =
        orderService.createOrder(createOrderRequest)

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun getOrderById(@PathVariable("id") id: Long) =
        orderService.getOrderById(id)

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/status/{status}")
    fun getOrdersByStatus(@PathVariable("status") orderStatus: OrderStatusDTO) =
        orderService.getOrdersByStatus(orderStatus)

    @GetMapping("/total-buys")
    @ResponseStatus(HttpStatus.OK)
    fun getProductsIdToQuantity(): Map<Long, Int> = orderService.getProductsIdToQuantity()

    @GetMapping("/{id}/details")
    @ResponseStatus(HttpStatus.OK)
    fun getOrderDetails(@PathVariable("id") id: Long) = orderService.getOrderDetails(id)

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = ["", "/"])
    fun getOrders(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "5") size: Int,
    ) = orderService.getOrders(page, size)

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun updateOrder(@PathVariable("id") id: Long, @RequestBody updateOrderRequest: UpdateOrderRequest) =
        orderService.updateOrder(id, updateOrderRequest)

    @PostMapping("/confirm")
    @ResponseStatus(HttpStatus.CREATED)
    fun confirmOrder(@RequestBody orderId: Long) =
        orderService.confirmOrder(orderId)

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
