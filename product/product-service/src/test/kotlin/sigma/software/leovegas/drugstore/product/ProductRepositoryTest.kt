package sigma.software.leovegas.drugstore.product

import java.math.BigDecimal
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.Pageable
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.infrastructure.extensions.get

@DisplayName("Product Repository test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductRepositoryTest @Autowired constructor(
    val transactionTemplate: TransactionTemplate,
    val productRepository: ProductRepository
) {

    @Test
    fun `should get products by name and status and quantity greater than`() {

        // given
        transactionTemplate.execute {
            productRepository.deleteAll()
        }

        // and
        transactionTemplate.execute {
            productRepository.saveAll(
                listOf(
                    Product(
                        name = "aspirin",
                        price = BigDecimal("10.00"),
                        quantity = 10,
                        status = ProductStatus.RECEIVED
                    ),
                    Product(
                        name = "test",
                        price = BigDecimal("50.00"),
                        quantity = 10,
                        status = ProductStatus.RECEIVED
                    ),
                    Product(
                        name = "aspirin",
                        price = BigDecimal("40.00"),
                        quantity = 10,
                        status = ProductStatus.CREATED
                    ),
                    Product(
                        name = "aspirin",
                        price = BigDecimal("30.00"),
                        quantity = 0,
                        status = ProductStatus.RECEIVED
                    )
                )
            )
        }.get()

        // when
        val products = productRepository.findAllByNameContainingAndStatusAndQuantityGreaterThan(
            "aspirin", ProductStatus.RECEIVED, 0, Pageable.unpaged()
        )

        // then
        assertThat(products).hasSize(1)
        assertThat(products[0].name).isEqualTo("aspirin")
        assertThat(products[0].quantity).isGreaterThan(0)
        assertThat(products[0].status).isEqualTo(ProductStatus.RECEIVED)
    }

    @Test
    fun `should get products by id in and status and quantity greater than`() {

        // given
        transactionTemplate.execute {
            productRepository.deleteAll()
        }

        // and
        val saved = transactionTemplate.execute {
            productRepository.saveAll(
                listOf(
                    Product(
                        name = "aspirin",
                        price = BigDecimal("10.00"),
                        quantity = 10,
                        status = ProductStatus.RECEIVED
                    ),
                    Product(
                        name = "test",
                        price = BigDecimal("50.00"),
                        quantity = 0,
                        status = ProductStatus.RECEIVED
                    ),
                    Product(
                        name = "aspirin",
                        price = BigDecimal("40.00"),
                        quantity = 10,
                        status = ProductStatus.CREATED
                    ),
                    Product(
                        name = "aspirin",
                        price = BigDecimal("30.00"),
                        quantity = 5,
                        status = ProductStatus.RECEIVED
                    )
                )
            )
        }.get()

        // when
        val products = productRepository.findAllByIdInAndStatusAndQuantityGreaterThan(
            setOf(saved[0].id ?: -1, saved[1].id ?: -1, saved[2].id ?: -1),
            ProductStatus.RECEIVED, 0, Pageable.unpaged()
        )

        // then
        assertThat(products).hasSize(1)
        assertThat(products[0].id).isIn(saved[0].id ?: -1, saved[1].id ?: -1, saved[2].id ?: -1)
        assertThat(products[0].quantity).isGreaterThan(0)
        assertThat(products[0].status).isEqualTo(ProductStatus.RECEIVED)
    }

    @Test
    fun `should get products by name and id in and status and quantity greater than`() {

        // given
        transactionTemplate.execute {
            productRepository.deleteAll()
        }

        // and
        val saved = transactionTemplate.execute {
            productRepository.saveAll(
                listOf(
                    Product(
                        name = "aspirin",
                        price = BigDecimal("10.00"),
                        quantity = 10,
                        status = ProductStatus.RECEIVED
                    ),
                    Product(
                        name = "test",
                        price = BigDecimal("50.00"),
                        quantity = 10,
                        status = ProductStatus.RECEIVED
                    ),
                    Product(
                        name = "aspirin",
                        price = BigDecimal("40.00"),
                        quantity = 10,
                        status = ProductStatus.CREATED
                    ),
                    Product(
                        name = "aspirin",
                        price = BigDecimal("30.00"),
                        quantity = 0,
                        status = ProductStatus.RECEIVED
                    ),
                    Product(
                        name = "aspirin",
                        price = BigDecimal("30.00"),
                        quantity = 0,
                        status = ProductStatus.RECEIVED
                    )
                )
            )
        }.get()

        // when
        val products = productRepository.findAllByNameContainingAndIdInAndStatusAndQuantityGreaterThan(
            "aspirin",
            setOf(saved[0].id ?: -1, saved[1].id ?: -1, saved[2].id ?: -1, saved[3].id ?: -1),
            ProductStatus.RECEIVED, 0, Pageable.unpaged()
        )

        // then
        assertThat(products).hasSize(1)
        assertThat(products[0].id).isIn(
            saved[0].id ?: -1,
            saved[1].id ?: -1,
            saved[2].id ?: -1,
            saved[3].id ?: -1
        )
        assertThat(products[0].quantity).isGreaterThan(0)
        assertThat(products[0].name).isEqualTo("aspirin")
        assertThat(products[0].status).isEqualTo(ProductStatus.RECEIVED)
    }
}
