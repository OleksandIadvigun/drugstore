package sigma.software.leovegas.drugstore.product

import java.math.BigDecimal
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.support.MessageBuilder
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.api.protobuf.Proto
import sigma.software.leovegas.drugstore.api.toDecimalProto
import sigma.software.leovegas.drugstore.product.api.CreateProductRequest
import sigma.software.leovegas.drugstore.product.api.CreateProductsEvent

@TestInstance(PER_CLASS)
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
        val productsToCreate =
            listOf(
                Proto.ProductDetailsItem.newBuilder()
                    .setProductNumber("1")
                    .setName("test1")
                    .setQuantity(1)
                    .setPrice(BigDecimal.ONE.toDecimalProto())
                    .build(),
                Proto.ProductDetailsItem.newBuilder()
                    .setProductNumber("2")
                    .setName("test2")
                    .setQuantity(2)
                    .setPrice(BigDecimal.TEN.toDecimalProto())
                    .build()
            )

        // and
        val createProtoEvent = Proto.CreateProductsEvent.newBuilder().addAllProducts(productsToCreate).build()

        // when
        val actual = channel.send(MessageBuilder.withPayload(createProtoEvent).build())

        // and
        val isEmpty = productRepository.findAllByProductNumberIn(listOf("1", "2")).isEmpty()

        // then
        assertThat(actual).isEqualTo(true)
        assertThat(isEmpty).isFalse
    }
}
