package sigma.software.leovegas.drugstore.service

import java.math.BigDecimal
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import sigma.software.leovegas.drugstore.controller.OrderController
import sigma.software.leovegas.drugstore.dto.OrderDetailsRequest
import sigma.software.leovegas.drugstore.dto.OrderDetailsResponse
import sigma.software.leovegas.drugstore.dto.OrderRequest
import sigma.software.leovegas.drugstore.dto.OrderResponse
import sigma.software.leovegas.drugstore.exception.InsufficientAmountOfProductForOrderException
import sigma.software.leovegas.drugstore.exception.OrderNotFoundException
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SpringBootTest
@AutoConfigureTestDatabase
class OrderServiceTest @Autowired constructor
    (private val orderService: OrderService) {

    @Test
    fun `should get order by id `() {

        // give
        val order = OrderResponse(
            id = 1,
            orderDetailsList = listOf(
                OrderDetailsResponse(
                    1L, "paracetamol", BigDecimal.valueOf(10.50), 3
                )
            ),
            total = BigDecimal.valueOf(31.50)
        )

        // when
        val received = orderService.getOrderById(1L)


        // then
        assertEquals(order, received)

    }

    @Test
    fun `should throw OrderKotlinNotFoundException`() {

        // then
        assertThrows<OrderNotFoundException> { orderService.getOrderById(-15L) }

        assertThrows<InsufficientAmountOfProductForOrderException> {
            orderService.updateOrder(0L, OrderRequest(emptyList()))
        }
    }

    @Test
    fun `should get all orders`() {

        // give
        val orderList = listOf(
            OrderResponse(
                id = 1,
                orderDetailsList = listOf(
                    OrderDetailsResponse(
                        1L, "paracetamol", BigDecimal.valueOf(10.50), 3
                    )
                ),
                total = BigDecimal.valueOf(31.50)
            ),
            OrderResponse(
                id = 2, orderDetailsList = listOf(
                    OrderDetailsResponse(
                        2L, "aspirin", BigDecimal.valueOf(5.7), 2
                    )
                ),
                total = BigDecimal.valueOf(11.40)
            )
        )

        // when
        val received = orderService.getAllOrders()

        // then
        assertEquals(orderList, received)
    }

    @Test
    fun `should create order with calculated total value for product`() {

        // give
        val order = OrderRequest(
            orderDetailsList = listOf(
                OrderDetailsRequest(
                    productId = 1L, quantity = 3
                )
            )
        )
        val total = BigDecimal.valueOf(31.50)

        //when
        val created = orderService.postOrder(order)

        //then
        assertTrue { (created.id != 0L) }
        assertEquals(total, created.total)

    }

    @Test
    fun `should throw InsufficientAmountOfProductForOrderException to create order`() {

        // then
        assertThrows<InsufficientAmountOfProductForOrderException> {
            orderService.postOrder(OrderRequest())
        }
        assertThrows<InsufficientAmountOfProductForOrderException> {
            orderService.updateOrder(1L, OrderRequest())
        }
    }

    @Test
    fun `should get changed order`() {
        // give
        val orderToChange = orderService.postOrder(
            OrderRequest(
                orderDetailsList = listOf(
                    OrderDetailsRequest(
                        productId = 1L, quantity = 4
                    )
                )
            )
        )

        val orderNew = OrderRequest(
            orderDetailsList = listOf(
                OrderDetailsRequest(
                    productId = 1L, quantity = 2
                )
            )
        )
        val total = BigDecimal.valueOf(21.0)

        //when
        val created = orderService.updateOrder(orderToChange.id, orderNew)

        //then
        assertEquals(orderNew.orderDetailsList[0].productId, created.orderDetailsList[0].productId)
        assertEquals(orderNew.orderDetailsList[0].quantity, created.orderDetailsList[0].quantity)
        assertEquals(total, created.total)

        //after
        orderService.cancelOrder(orderToChange.id)
    }

    @Test
    fun `should delete order`() {
        // give
        val orderIdToDelete = 2L
        // when
        orderService.cancelOrder(orderIdToDelete)
        // then
        assertThrows<OrderNotFoundException> { orderService.getOrderById(orderIdToDelete) }
    }
}
