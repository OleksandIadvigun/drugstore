package sigma.software.leovegas.drugstore.product

import java.math.BigDecimal
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.support.MessageBuilder
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.product.api.CreateProductRequest
import sigma.software.leovegas.drugstore.product.api.CreateProductsEvent

@DisplayName("Rabbitmq test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RabbitmqTest @Autowired constructor(
    val transactionTemplate: TransactionTemplate,
    val productRepository: ProductRepository,
    @Qualifier("createProductEventHandler-in-0") val channel: MessageChannel
) {

    @Test
    fun `should create products`() {
        // setup
        transactionTemplate.execute { productRepository.deleteAll() }

        // given
        val productRequest = CreateProductsEvent(
            listOf(
                CreateProductRequest(
                    productNumber = "1",
                    name = "test1",
                    quantity = 1,
                    price = BigDecimal.ONE
                ),
                CreateProductRequest(
                    productNumber = "2",
                    name = "test2",
                    quantity = 2,
                    price = BigDecimal.TEN
                )
            )
        )

        // when
        val actual = channel.send(MessageBuilder.withPayload(productRequest).build())

        // and
        val isEmpty = productRepository.findAllByProductNumberIn(listOf("1", "2")).isEmpty()
        // when
        assertThat(actual).isEqualTo(true)
        assertThat(isEmpty).isFalse
    }
}
