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
                        productNumber = "1",
                        name = "aspirin",
                        price = BigDecimal("10.00"),
                        quantity = 10,
                        status = ProductStatus.RECEIVED
                    ),
                    Product(
                        productNumber = "2",
                        name = "test",
                        price = BigDecimal("50.00"),
                        quantity = 10,
                        status = ProductStatus.RECEIVED
                    ),
                    Product(
                        productNumber = "3",
                        name = "aspirin",
                        price = BigDecimal("40.00"),
                        quantity = 10,
                        status = ProductStatus.CREATED
                    ),
                    Product(
                        productNumber = "4",
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
    fun `should get products by product numbers in and status and quantity greater than`() {

        // given
        transactionTemplate.execute {
            productRepository.deleteAll()
        }

        // and
        val saved = transactionTemplate.execute {
            productRepository.saveAll(
                listOf(
                    Product(
                        productNumber = "1",
                        name = "aspirin",
                        price = BigDecimal("10.00"),
                        quantity = 10,
                        status = ProductStatus.RECEIVED
                    ),
                    Product(
                        productNumber = "2",
                        name = "test",
                        price = BigDecimal("50.00"),
                        quantity = 0,
                        status = ProductStatus.RECEIVED
                    ),
                    Product(
                        productNumber = "3",
                        name = "aspirin",
                        price = BigDecimal("40.00"),
                        quantity = 10,
                        status = ProductStatus.CREATED
                    ),
                    Product(
                        productNumber = "4",
                        name = "aspirin",
                        price = BigDecimal("30.00"),
                        quantity = 5,
                        status = ProductStatus.RECEIVED
                    )
                )
            )
        }.get()

        // when
        val products = productRepository.findAllByProductNumberInAndStatusAndQuantityGreaterThan(
            setOf(saved[0].productNumber, saved[1].productNumber, saved[2].productNumber),
            ProductStatus.RECEIVED, 0, Pageable.unpaged()
        )

        // then
        assertThat(products).hasSize(1)
        assertThat(products[0].productNumber).isIn(saved[0].productNumber, saved[1].productNumber, saved[2].productNumber)
        assertThat(products[0].quantity).isGreaterThan(0)
        assertThat(products[0].status).isEqualTo(ProductStatus.RECEIVED)
    }

    @Test
    fun `should get products by name and product numbers in and status and quantity greater than`() {

        // given
        transactionTemplate.execute {
            productRepository.deleteAll()
        }

        // and
        val saved = transactionTemplate.execute {
            productRepository.saveAll(
                listOf(
                    Product(
                        productNumber = "1",
                        name = "aspirin",
                        price = BigDecimal("10.00"),
                        quantity = 10,
                        status = ProductStatus.RECEIVED
                    ),
                    Product(
                        productNumber = "2",
                        name = "test",
                        price = BigDecimal("50.00"),
                        quantity = 10,
                        status = ProductStatus.RECEIVED
                    ),
                    Product(
                        productNumber = "3",
                        name = "aspirin",
                        price = BigDecimal("40.00"),
                        quantity = 10,
                        status = ProductStatus.CREATED
                    ),
                    Product(
                        productNumber = "4",
                        name = "aspirin",
                        price = BigDecimal("30.00"),
                        quantity = 0,
                        status = ProductStatus.RECEIVED
                    ),
                    Product(
                        productNumber = "5",
                        name = "aspirin",
                        price = BigDecimal("30.00"),
                        quantity = 0,
                        status = ProductStatus.RECEIVED
                    )
                )
            )
        }.get()

        // when
        val products = productRepository.findAllByNameContainingAndProductNumberInAndStatusAndQuantityGreaterThan(
            "aspirin",
            setOf(saved[0].productNumber, saved[1].productNumber, saved[2].productNumber, saved[3].productNumber),
            ProductStatus.RECEIVED, 0, Pageable.unpaged()
        )

        // then
        assertThat(products).hasSize(1)
        assertThat(products[0].productNumber).isIn(
            saved[0].productNumber,
            saved[1].productNumber,
            saved[2].productNumber,
            saved[3].productNumber
        )
        assertThat(products[0].quantity).isGreaterThan(0)
        assertThat(products[0].name).isEqualTo("aspirin")
        assertThat(products[0].status).isEqualTo(ProductStatus.RECEIVED)
    }

    @Test
    fun `should get products by products numbers`() {

        // given
        transactionTemplate.execute {
            productRepository.deleteAll()
        }

        // and
        transactionTemplate.execute {
            productRepository.saveAll(
                listOf(
                    Product(
                        productNumber = "1",
                        name = "aspirin",
                        price = BigDecimal("10.00"),
                        quantity = 10,
                        status = ProductStatus.RECEIVED
                    ),
                    Product(
                        productNumber = "2",
                        name = "test",
                        price = BigDecimal("50.00"),
                        quantity = 10,
                        status = ProductStatus.RECEIVED
                    ),
                )
            )
        }.get()

        // when
        val products = productRepository.findAllByProductNumberIn(listOf("1", "2"))

        // then
        assertThat(products).hasSize(2)
        assertThat(products[0].productNumber).isEqualTo("1")
        assertThat(products[1].productNumber).isEqualTo("2")
    }
}
