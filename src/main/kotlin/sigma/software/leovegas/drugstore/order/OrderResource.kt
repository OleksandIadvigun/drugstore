package sigma.software.leovegas.drugstore.order

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/orders")
class OrderResource (@Autowired private val orderService: OrderService) {

    @PostMapping(path = ["", "/"])
    @ResponseStatus(HttpStatus.CREATED)
    fun createOrder(@RequestBody orderRequest: OrderRequest) =
        orderService.createOrder(orderRequest)

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun getOrderById(@PathVariable("id") id: Long) =
        orderService.getOrderById(id)

    @GetMapping(path = ["", "/"])
    @ResponseStatus(HttpStatus.OK)
    fun getOrders() =
        orderService.getOrders()

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun updateOrder(@PathVariable("id") id: Long, @RequestBody orderRequest: OrderRequest) =
        orderService.updateOrder(id, orderRequest)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteOrder(@PathVariable("id") id: Long) =
        orderService.deleteOrder(id)
}
