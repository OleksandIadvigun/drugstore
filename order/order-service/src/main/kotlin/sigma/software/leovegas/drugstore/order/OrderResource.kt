package sigma.software.leovegas.drugstore.order

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import sigma.software.leovegas.drugstore.api.ApiError
import sigma.software.leovegas.drugstore.order.api.CreateOrderRequest
import sigma.software.leovegas.drugstore.order.api.OrderStatusDTO
import sigma.software.leovegas.drugstore.order.api.UpdateOrderRequest

@RestController
@RequestMapping("/api/v1/orders")
class OrderResource(private val orderService: OrderService) {

    @PostMapping(path = ["", "/"])
    @ResponseStatus(HttpStatus.CREATED)
    fun createOrder(@RequestBody createOrderRequest: CreateOrderRequest) =
        orderService.createOrder(createOrderRequest)

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun getOrderById(@PathVariable("id") id: Long) =
        orderService.getOrderById(id)

    @GetMapping("/status/{status}")
    @ResponseStatus(HttpStatus.OK)
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
    fun getOrders() = orderService.getOrders()

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun updateOrder(@PathVariable("id") id: Long, @RequestBody updateOrderRequest: UpdateOrderRequest) =
        orderService.updateOrder(id, updateOrderRequest)

    @PutMapping("/change-status/{id}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun changeOrderStatus(@PathVariable("id") id: Long, @RequestBody orderStatus: OrderStatusDTO) =
        orderService.changeOrderStatus(id, orderStatus)

    @PostMapping("/confirm")
    @ResponseStatus(HttpStatus.CREATED)
    fun confirmOrder(@RequestBody orderId: Long) =
        orderService.confirmOrder(orderId)

    @ExceptionHandler(Throwable::class)
    fun handleNotFound(e: Throwable) = run {
        val status = when (e) {
            is InsufficientAmountOfOrderItemException -> HttpStatus.BAD_REQUEST
            is OrderNotFoundException -> HttpStatus.BAD_REQUEST
            is OrderNotCreatedException -> HttpStatus.BAD_REQUEST
            is AccountancyServerNotAvailable -> HttpStatus.GATEWAY_TIMEOUT
            is OrderStatusException -> HttpStatus.BAD_REQUEST
            is ProductServerNotAvailable -> HttpStatus.GATEWAY_TIMEOUT
            else -> HttpStatus.BAD_REQUEST
        }
        ResponseEntity.status(status).body(ApiError(status.value(), status.name, e.message))
    }
}
