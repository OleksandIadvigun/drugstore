package sigma.software.leovegas.drugstore.service

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import sigma.software.leovegas.drugstore.dto.ProductRequest
import sigma.software.leovegas.drugstore.dto.ProductResponse
import sigma.software.leovegas.drugstore.exception.ResourceNotFoundException
import java.math.BigDecimal


@SpringBootTest
@AutoConfigureTestDatabase
class ProductKotlinServiceTest() {
    val productRequest = ProductRequest(name = "test", price = BigDecimal("25.5"), quantity = 5)
    val productResponse = ProductResponse(name = "test", price = BigDecimal("25.5"), quantity = 5)

    @Autowired
    lateinit var service: ProductServiceImpl

    @Test
    fun `get all should return List of productsResponse`() {
        val all = service.getAll()

        Assertions.assertNotNull(all)
        Assertions.assertTrue(all::class.qualifiedName == "java.util.ArrayList")
    }

    @Test
    fun `if exist should return productDto`() {
        val actual = service.getOne(1)

        Assertions.assertNotNull(actual)
        Assertions.assertTrue(actual.javaClass.kotlin == ProductResponse::class)
    }

    @Test
    fun `if not exist should return ResourceNotFoundException`() {
        assertThrows<ResourceNotFoundException> {
            service.getOne(Long.MAX_VALUE)
        }
    }

    @Test
    fun `when create should return productDto`() {
        val actual = service.save(productRequest)

        Assertions.assertNotNull(actual)
        Assertions.assertNotNull(actual.id)
        Assertions.assertEquals(productResponse.name, actual.name)
        Assertions.assertEquals(productResponse.price, actual.price)
        Assertions.assertEquals(productResponse.quantity, actual.quantity)
    }

    @Test
    fun `when update should return updated productDto`() {
        val randomName = Math.random().toString()
        productRequest.name = randomName
        val actual = service.update(1, productRequest)

        Assertions.assertNotNull(actual)
        Assertions.assertEquals(randomName, actual.name)
    }

    @Test
    fun `if updated product not exist should return ResourceNotFoundException`() {
        assertThrows<ResourceNotFoundException> {
            service.update(Long.MAX_VALUE, productRequest)
        }
    }

    @Test
    fun `if exist should delete product`() {
        val product = service.save(productRequest)
        val response = service.delete(product.id)

        Assertions.assertEquals("kotlin.Unit", response::class.qualifiedName)
    }

}

