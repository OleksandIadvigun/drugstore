package sigma.software.leovegas.drugstore.product

import java.math.BigDecimal
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.hibernate.validator.internal.util.Contracts.assertNotNull
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.product.api.ProductRequest
import sigma.software.leovegas.drugstore.product.api.ProductResponse

@SpringBootTest
@AutoConfigureTestDatabase
@DisplayName("ProductService test")
class ProductServiceTest @Autowired constructor(
    val service: ProductService,
    val transactionTemplate: TransactionTemplate,
    val repository: ProductRepository
) {

    @Test
    fun `should get products`() {

        // given
        transactionTemplate.execute {
            repository.deleteAllInBatch()
        }

        // and
        transactionTemplate.execute {
            repository.saveAll(
                listOf(
                    Product(
                        name = "test1",
                        price = BigDecimal("20.00")
                    ),
                    Product(
                        name = "test2",
                        price = BigDecimal("40.00")
                    )
                )
            )
        }

        // when
        val all = service.getAll()

        // then
        assertNotNull(all)
        assertThat(all).hasSize(2)
    }

    @Test
    fun `should get products by ids`() {

        // given
        transactionTemplate.execute {
            repository.deleteAllInBatch()
        }

        // and
        val ids  = transactionTemplate.execute {
           repository.saveAll(
                listOf(
                    Product(
                        name = "test1",
                        price = BigDecimal("20.00")
                    ),
                    Product(
                        name = "test2",
                        price = BigDecimal("40.00")
                    )
                )
            ).map { it.id }
        }

        // when
        val products = service.getProductsByIds(ids as List<Long>)

        // then
        assertThat(products).hasSize(2)
        assertThat(products[0].name).isEqualTo("test1")
        assertThat(products[1].name).isEqualTo("test2")
    }

    @Test
    fun `should get product by id`() {

        // given
        val productRequest = ProductRequest(
            name = "test",
            price = BigDecimal("25.50"),
        )

        // and
        val saved = transactionTemplate.execute {
            repository.save(productRequest.toEntity()).toProductResponse()
        } ?: fail("result is expected")

        // when
        val actual = service.getOne(saved.id)

        // then
        assertNotNull(actual)
        assertThat(actual.id).isEqualTo(saved.id)
        assertThat(actual.name).isEqualTo(saved.name)
        assertThat(actual.price).isEqualTo(saved.price)
    }

    @Test
    fun `should not get not exist product`() {

        // given
        val id = Long.MAX_VALUE

        // when
        val exception = assertThrows<ResourceNotFoundException> {
            service.getOne(id)
        }

        //then
        assertThat(exception.message).isEqualTo("This product with id: $id doesn't exist!")
    }

    @Test
    fun `should create product`() {

        // given
        val productRequest = ProductRequest(
            name = "test",
            price = BigDecimal("25.50"),
        )

        //and
        val productResponse = ProductResponse(
            name = "test",
            price = BigDecimal("25.50"),
        )

        // when
        val actual = service.create(productRequest)

        // then
        assertNotNull(actual)
        assertNotNull(actual.id)
        assertEquals(productResponse.name, actual.name)
        assertEquals(productResponse.price, actual.price)
    }

    @Test
    fun `should update product`() {

        // given
        val productRequest = ProductRequest(
            name = "test",
            price = BigDecimal("25.50"),
        )

        // and
        val saved = transactionTemplate.execute {
            repository.save(productRequest.toEntity()).toProductResponse()
        } ?: fail("result is expected")

        // and
        val randomName = Math.random().toString()
        val updatedProductRequest = ProductRequest(
            name = randomName,
            price = BigDecimal("25.50"),
        )

        // when
        val actual = service.update(saved.id, updatedProductRequest)

        // then
        assertNotNull(actual)
        assertEquals(randomName, actual.name)
    }

    @Test
    fun `should not update not existing product`() {

        // given
        val id = Long.MAX_VALUE

        // and
        val productRequest = ProductRequest(
            name = "test",
            price = BigDecimal("25.50"),
        )

        // when
        val exception = assertThrows<ResourceNotFoundException> {
            service.update(Long.MAX_VALUE, productRequest)
        }

        // then
        assertThat(exception.message).isEqualTo("This product with id: $id doesn't exist!")
    }

    @Test
    fun `should delete product`() {

        // given
        val productRequest = ProductRequest(
            name = "test",
            price = BigDecimal("25.50"),
        )

        // and
        val product = service.create(productRequest)

        // when
        service.delete(product.id)

        // then
        val exception = assertThrows<ResourceNotFoundException> {
            service.getOne(product.id)
        }

        //and
        assertThat(exception.message).isEqualTo("This product with id: ${product.id} doesn't exist!")
    }
}
