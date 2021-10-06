package sigma.software.leovegas.drugstore.order

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.infrastructure.extensions.get

@DisplayName("Order Repository test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderRepositoryTest @Autowired constructor(
    val transactionTemplate: TransactionTemplate,
    val orderRepository: OrderRepository,
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
                    orderNumber = "1",
                    orderItems = setOf(
                        OrderItem(
                            productNumber = " 1",
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
                        orderNumber = "1",
                        orderStatus = OrderStatus.CONFIRMED,
                        orderItems = setOf(
                            OrderItem(
                                productNumber = "1",
                                quantity = 3
                            )
                        )
                    ),
                    Order(
                        orderNumber = "2",
                        orderStatus = OrderStatus.CONFIRMED,
                        orderItems = setOf(
                            OrderItem(
                                productNumber = "1",
                                quantity = 1
                            )
                        )
                    ),
                    Order(
                        orderNumber = "3",
                        orderStatus = OrderStatus.CONFIRMED,
                        orderItems = setOf(
                            OrderItem(
                                productNumber = "2",
                                quantity = 5
                            )
                        )
                    ),
                )
            )
        }.get()

        val productNumbers = created.map { it.orderItems.map { item -> item.productNumber } }.flatten()

        // when
        val actual = transactionTemplate.execute {
            orderRepository.getProductNumberToQuantity(productNumbers)
        }.get()

        //then
        assertThat(actual).hasSize(2)
        assertThat(actual[0].productNumber).isEqualTo("2")
        assertThat(actual[0].quantity).isEqualTo(5)
        assertThat(actual[1].productNumber).isEqualTo("1")
        assertThat(actual[1].quantity).isEqualTo(4)
    }

    @Test
    fun `should get order by order number`() {

        // setup
        transactionTemplate.execute {
            orderRepository.deleteAll()
        }

        // given
        val saved = transactionTemplate.execute {
            orderRepository.save(
                Order(
                    orderNumber = "1",
                    orderStatus = OrderStatus.CONFIRMED,
                    orderItems = setOf(
                        OrderItem(
                            productNumber = "1",
                            quantity = 1
                        )
                    )
                )
            )
        }.get()

        // when
        val actual = orderRepository.findByOrderNumber(saved.orderNumber).get()

        // then
        assertThat(actual.orderNumber).isEqualTo(saved.orderNumber)
    }
}
