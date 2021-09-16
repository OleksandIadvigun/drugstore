package sigma.software.leovegas.drugstore.order.client

import feign.Headers
import feign.Param
import feign.RequestLine
import org.springframework.web.bind.annotation.RequestBody
import sigma.software.leovegas.drugstore.accountancy.api.ConfirmOrderResponse
import sigma.software.leovegas.drugstore.order.api.CreateOrderRequest
import sigma.software.leovegas.drugstore.order.api.OrderDetailsDTO
import sigma.software.leovegas.drugstore.order.api.OrderResponse
import sigma.software.leovegas.drugstore.order.api.OrderStatusDTO
import sigma.software.leovegas.drugstore.order.api.UpdateOrderRequest

@Headers("Content-Type: application/json")
interface OrderClient {

    @RequestLine("POST /api/v1/orders")
    fun createOrder(request: CreateOrderRequest): OrderResponse

    @RequestLine("PUT /api/v1/orders/{orderNumber}")
    fun updateOrder(@Param("orderNumber") orderNumber: String, request: UpdateOrderRequest): OrderResponse

    @RequestLine("GET /api/v1/orders/{orderNumber}")
    fun getOrderById(@Param("orderNumber") orderNumber: String): OrderResponse

    @RequestLine("GET /api/v1/orders/status/{status}")
    fun getOrdersByStatus(@Param("status") orderStatus: OrderStatusDTO): List<OrderResponse>

    @RequestLine("GET /api/v1/orders/{orderNumber}/details")
    fun getOrderDetails(@Param("orderNumber") orderNumber: String): OrderDetailsDTO

    @RequestLine("GET /api/v1/orders?page={page}&size={size}")
    fun getOrders(
        @Param("page") page: Int = 0,
        @Param("size") size: Int = 5
    ): List<OrderResponse>

    @RequestLine("GET /api/v1/orders/total-buys")
    fun getProductsIdToQuantity(): Map<String, Int>

    @RequestLine("POST /api/v1/orders/confirm")
    fun confirmOrder(@RequestBody orderNumber: String): ConfirmOrderResponse
}
