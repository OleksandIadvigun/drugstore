package sigma.software.leovegas.drugstore.order

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.transaction.support.TransactionTemplate
import kotlin.test.fail

@AutoConfigureTestDatabase
@SpringBootTest(webEnvironment = RANDOM_PORT)
@DisplayName("OrderService test")
class OrderServiceTest @Autowired constructor(
    val transactionTemplate: TransactionTemplate,
    val orderService: OrderService,
    val orderRepository: OrderRepository
) {

    @Test
    fun `should get order by id `() {

    // given

        val orderCreated = transactionTemplate.execute {
            orderRepository.save(
                Order(
                    orderItems = setOf(
                        OrderItem(
                            productId = 1L,
                            quantity = 3
                        )
                    )
                )
            )
        }?.toOrderResponse() ?: fail("result is expected")


        // when
        val orderActual = transactionTemplate.execute {
            orderService.getOrderById(orderCreated.id!!)
        }

        // then
        assertThat(orderActual?.id).isEqualTo(orderCreated.id)
        assertThat(orderActual?.orderItems!!.elementAt(0).productId)
            .isEqualTo(orderCreated.orderItems.elementAt(0).productId)
        assertThat(orderActual.orderItems.elementAt(0).quantity)
            .isEqualTo(orderCreated.orderItems.elementAt(0).quantity)
    }

    @Test
    fun `if get order and not exist should throw OrderKotlinNotFoundException`() {

        // given
        val id = -15L //invalid id

        // then
        assertThrows<OrderNotFoundException> { orderService.getOrderById(id) }
    }

    @Test
    fun `if change order and not exist should throw OrderKotlinNotFoundException`() {

        // given
        val id = -15L //invalid id

        // then
        assertThrows<OrderNotFoundException> { orderService.updateOrder(id, OrderRequest(emptySet())) }
    }

    @Test
    fun `should get all orders`() {

        // when
        val orders = transactionTemplate.execute {
            orderService.getOrders()
        } ?: fail("result is expected")

        // then
        assertThat(orders).isNotEmpty
        assertTrue(orders::class.qualifiedName == "java.util.ArrayList")
    }

    @Test
    fun `should create order`() {

        // given
        val order = OrderRequest(
            setOf(
                OrderItem(
                    productId = 1L,
                    quantity = 3
                )
            )
        )

        // when
        val created = transactionTemplate.execute {
            orderService.createOrder(order)
        } ?: fail("result is expected")

        // then
        assertThat(created.id).isNotNull
    }

    @Test
    fun `should throw InsufficientAmountOfProductForOrderException to create order`() {

        // given
        val orderToUpdate = transactionTemplate.execute {
            orderRepository.save(
                Order(
                    orderItems = setOf(
                        OrderItem(
                            productId = 1L,
                            quantity = 3
                        )
                    ),
                )
            )
        }?.toOrderResponse() ?: fail("result is expected")
        // then
        assertThrows<InsufficientAmountOfOrderItemException> {
            orderService.createOrder(OrderRequest(emptySet()))
        }

        assertThrows<InsufficientAmountOfOrderItemException> {
            orderService.updateOrder(orderToUpdate.id!!, OrderRequest(emptySet()))
        }
    }

    @Test
    fun `should update order`() {

        // given
        val orderToChange = transactionTemplate.execute {
            orderRepository.save(
                Order(
                    orderItems = setOf(
                        OrderItem(
                            productId = 1L,
                            quantity = 3
                        )
                    ),
                )
            )
        }?.toOrderResponse() ?: fail("result is expected")

        // and
        val orderRequest = OrderRequest(
            orderItems = setOf(
                OrderItem(
                    productId = 1L,
                    quantity = 4
                )
            )
        )

        // when
        val changedOrder = transactionTemplate.execute {
            orderService.updateOrder(orderToChange.id!!, orderRequest)
        } ?: fail("result is expected")

        // then
        assertThat(changedOrder.orderItems.elementAt(0).quantity)
            .isEqualTo(orderRequest.orderItems.elementAt(0).quantity)
    }

    @Test
    fun `should delete order`() {

        // given
        val orderToChange = transactionTemplate.execute {
            orderRepository.save(
                Order(
                    orderItems = setOf(
                        OrderItem(
                            productId = 1L,
                            quantity = 3
                        )
                    ),
                )
            )
        }?.toOrderResponse() ?: fail("result is expected")

        // when
        transactionTemplate.execute {
            orderService.deleteOrder(orderToChange.id!!)
        } ?: fail("result is expected")

        // then
        assertThrows<OrderNotFoundException> {
            transactionTemplate.execute {
                orderService.getOrderById(orderToChange.id!!)
            }
        }
    }
}
