package sigma.software.leovegas.drugstore.order

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.support.TransactionTemplate

@DisplayName("Order Repository test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderRepositoryTest(
    @Autowired val transactionalTemplate: TransactionTemplate,
    @Autowired val orderRepository: OrderRepository
) {

    @Test
    fun `should get views`() {

        // given
        transactionalTemplate.execute {
            orderRepository.deleteAll()
        }

        // and
        val created = transactionalTemplate.execute {
            orderRepository.saveAll(
                listOf(
                    Order(
                        orderItems = setOf(
                            OrderItem(
                                productId = 1,
                                quantity = 3
                            )
                        )
                    ),
                    Order(
                        orderItems = setOf(
                            OrderItem(
                                productId = 1,
                                quantity = 1
                            )
                        )
                    ),
                    Order(
                        orderItems = setOf(
                            OrderItem(
                                productId = 2,
                                quantity = 5
                            )
                        )
                    )
                )
            )
        } ?: fail("result is expected")

        // when
        val actual = transactionalTemplate.execute {
            orderRepository.getIdToQuantity()
        } ?: fail("result is expected")

        //then
        assertThat(actual).hasSize(2)
        assertThat(actual[0].quantity).isEqualTo(5)
        assertThat(actual[0].productId).isEqualTo(2)
    }
}
