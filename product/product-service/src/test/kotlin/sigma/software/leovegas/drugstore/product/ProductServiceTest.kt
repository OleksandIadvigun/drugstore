package sigma.software.leovegas.drugstore.product

import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
@AutoConfigureTestDatabase
@DisplayName("ProductService test")
class ProductServiceTest(@Autowired val service: ProductService) {

    @Test
    fun `should return List of productsResponse`() {

        // when
        val all = service.getAll()

        // then
        assertNotNull(all)
        assertTrue(all::class.qualifiedName == "java.util.ArrayList")
    }

    @Test
    fun `if exist should return productResponse`() {

        // given
        val productRequest = ProductRequest(
            name = "test",
            price = BigDecimal("25.50"),
        )

        // and
        val saved = service.create(productRequest)

        // when
        val actual = service.getOne(saved.id!!)

        // then
        assertNotNull(actual)
        assertThat(actual.id).isEqualTo(saved.id)
        assertThat(actual.name).isEqualTo(saved.name)
        assertThat(actual.price).isEqualTo(saved.price)
    }

    @Test
    fun `if not exist should return ResourceNotFoundException`() {
        assertThrows<ResourceNotFoundException> {
            service.getOne(Long.MAX_VALUE)
        }
    }

    @Test
    fun `should create product and return productResponse`() {

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
    fun `when update should return updated productResponse`() {

        // given
        val productRequest = ProductRequest(
            name = "test",
            price = BigDecimal("25.50"),
        )

        // and
        val saved = service.create(productRequest)
        val randomName = Math.random().toString()
        val updatedProductRequest = ProductRequest(
            name = randomName,
            price = BigDecimal("25.50"),
        )

        // when
        val actual = service.update(saved.id!!, updatedProductRequest)

        // then
        assertNotNull(actual)
        assertEquals(randomName, actual.name)
    }

    @Test
    fun `if updated product not exist should return ResourceNotFoundException`() {

        // given
        val productRequest = ProductRequest(
            name = "test",
            price = BigDecimal("25.50"),
        )

        //then
        assertThrows<ResourceNotFoundException> {
            service.update(Long.MAX_VALUE, productRequest)
        }
    }

    @Test
    fun `if exist should delete product`() {

        // given
        val productRequest = ProductRequest(
            name = "test",
            price = BigDecimal("25.50"),
        )

        // and
        val product = service.create(productRequest)

        // when
        service.delete(product.id!!)

        // then
        assertThrows<ResourceNotFoundException> {
            service.getOne(product.id!!)
        }
    }

    @Test
    fun `if field price is more than 10000000 should return NotCorrectRequestException`() {

        // given
        val productRequest = ProductRequest(
            name = "test",
            price = BigDecimal("20000000"),
        )

        // then
        assertThrows<NotCorrectRequestException> {
            service.update(1L, productRequest)
        }
    }
}
