package sigma.software.leovegas.drugstore.order

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.support.TransactionTemplate

@DisplayName("Order Repository test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderRepositoryTest @Autowired constructor(
    val transactionTemplate: TransactionTemplate,
    val orderRepository: OrderRepository
) {

    @Test
    fun `should get order by status`() {

        // given
        transactionTemplate.execute {
            orderRepository.deleteAll()
        }

        // and
        transactionTemplate.execute {
            orderRepository.save(
                Order(
                    orderItems = setOf(
                        OrderItem(
                            productId = 1,
                            quantity = 3
                        )
                    ),
                    orderStatus = OrderStatus.CREATED
                )
            )
        }?.toOrderResponseDTO() ?: fail("result is expected")

        // when
        val changed = transactionTemplate.execute {
            orderRepository.getAllByOrderStatus(OrderStatus.CREATED)
        } ?: fail("result is expected")

        // given
        assertThat(changed[0].orderStatus).isEqualTo(OrderStatus.CREATED)
    }

    @Test
    fun `should get views`() {

        // given
        transactionTemplate.execute {
            orderRepository.deleteAll()
        }

        // and
        val created = transactionTemplate.execute {
            orderRepository.saveAll(
                listOf(
                    Order(
                        orderStatus = OrderStatus.CONFIRMED,
                        orderItems = setOf(
                            OrderItem(
                                productId = 1,
                                quantity = 3
                            )
                        )
                    ),
                    Order(
                        orderStatus = OrderStatus.CONFIRMED,
                        orderItems = setOf(
                            OrderItem(
                                productId = 1,
                                quantity = 1
                            )
                        )
                    ),
                    Order(
                        orderStatus = OrderStatus.CONFIRMED,
                        orderItems = setOf(
                            OrderItem(
                                productId = 2,
                                quantity = 5
                            )
                        )
                    ),
                )
            )
        } ?: fail("result is expected")

        val ids = created.map { it.orderItems.map { item -> item.id ?: -1 } }.flatten()

        // when
        val actual = transactionTemplate.execute {
            orderRepository.getIdToQuantity(ids)
        } ?: fail("result is expected")

        //then
        assertThat(actual).hasSize(2)
        assertThat(actual[0].priceItemId).isEqualTo(2)
        assertThat(actual[0].quantity).isEqualTo(5)
        assertThat(actual[1].priceItemId).isEqualTo(1)
        assertThat(actual[1].quantity).isEqualTo(4)
    }
}
