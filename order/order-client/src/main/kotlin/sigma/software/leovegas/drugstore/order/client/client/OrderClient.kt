package sigma.software.leovegas.drugstore.order.client.client

import feign.Headers
import feign.Param
import feign.RequestLine
import sigma.software.leovegas.drugstore.order.api.CreateOrderRequest
import sigma.software.leovegas.drugstore.order.api.OrderDetailsDTO
import sigma.software.leovegas.drugstore.order.api.OrderResponse
import sigma.software.leovegas.drugstore.order.api.UpdateOrderRequest

@Headers("Content-Type: application/json")
interface OrderClient {

    @RequestLine("POST /api/v1/orders")
    fun createOrder(request: CreateOrderRequest): OrderResponse

    @RequestLine("PUT /api/v1/orders/{id}")
    fun updateOrder(@Param("id") id: Long, request: UpdateOrderRequest): OrderResponse

    @RequestLine("GET /api/v1/orders/{id}")
    fun getOrderById(@Param("id") id: Long): OrderResponse

    @RequestLine("GET /api/v1/orders/{id}/details")
    fun getOrderDetails(@Param("id") id: Long): OrderDetailsDTO

    @RequestLine("GET /api/v1/orders")
    fun getOrders(): List<OrderResponse>

    @RequestLine("GET /api/v1/orders/total-buys")
    fun getProductsIdToQuantity(): Map<Long, Int>
}
