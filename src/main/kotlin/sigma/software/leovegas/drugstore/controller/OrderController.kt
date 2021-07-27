package sigma.software.leovegas.drugstore.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import sigma.software.leovegas.drugstore.dto.OrderRequest
import sigma.software.leovegas.drugstore.dto.OrderResponse
import sigma.software.leovegas.drugstore.service.OrderService

@RestController
@RequestMapping("/orders")
class OrderController @Autowired constructor(private val orderService: OrderService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun postOrder(@RequestBody orderRequest: OrderRequest): OrderResponse = orderService.postOrder(orderRequest)

    @DeleteMapping(path = ["/{id}"])
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun cancelOrder(@PathVariable("id") id: Long) = orderService.cancelOrder(id)

    @PostMapping(path = ["/{id}"])
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun updateOrder(@PathVariable("id") id: Long, @RequestBody orderRequest: OrderRequest): OrderResponse =
        orderService.updateOrder(id, orderRequest)

    @GetMapping(path = ["/{id}"])
    @ResponseStatus(HttpStatus.OK)
    fun getOrder(@PathVariable("id") id: Long): OrderResponse = orderService.getOrderById(id)

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun getAllOrders(): List<OrderResponse> = orderService.getAllOrders()

}
