package sigma.software.leovegas.drugstore.product

import java.math.BigDecimal
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.transaction.support.TransactionTemplate

@DisplayName("ProductRepository test")
@SpringBootTest(webEnvironment = RANDOM_PORT)
class ProductRepositoryTest @Autowired constructor(
    val transactionTemplate: TransactionTemplate,
    val productRepository: ProductRepository
) {

    @Test
    fun `should find product views`() {

        // given
        val createdProductIds = listOf(
            1 to BigDecimal.ONE,
            2 to BigDecimal.TEN
        ).map {
            Product(
                name = "${it.first} test name",
                quantity = it.first,
                price = it.second
            )
        }.map { p ->
            transactionTemplate.execute {
                productRepository.save(p)
            }
        }.map{
            it?.id ?: fail("it may not be null")
        }



        // when
        val views = productRepository.findProductsView(createdProductIds)

        // then
        assertThat(views).hasSize(2)

        // and
        assertThat(views[0].price).isEqualTo(BigDecimal.ONE.setScale(2))
        assertThat(views[1].price).isEqualTo(BigDecimal.TEN.setScale(2))


    }
}
