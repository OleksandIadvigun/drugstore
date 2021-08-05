package sigma.software.leovegas.drugstore.product

import java.math.BigDecimal
import kotlin.test.assertEquals
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Product converters test")
class ProductConvertersTest {

    @Test
    fun `should convert to ProductResponse`() {

        // given
        val product = Product(name = "test", price = BigDecimal.TEN)
        val productResponse = ProductResponse(name = "test", price = BigDecimal.TEN)

        // when
        val actual = product.convertToProductResponse()

        // then
        assertEquals(productResponse, actual)
    }

    @Test
    fun `should convert to Product`() {

        // given
        val product = Product(name = "test", price = BigDecimal.TEN)
        val productRequest = ProductRequest(name = "test", price = BigDecimal.TEN)

        // when
        val actual = productRequest.convertToProduct()

        // then
        assertEquals(product, actual)
    }

    @Test
    fun `should convert list of products to list of DTOs`() {

        // given
         val product = Product(name = "test", price = BigDecimal.TEN)
         val productResponse = ProductResponse(name = "test", price = BigDecimal.TEN)
        val products = mutableListOf(product, product)

        // when
        val actual = products.convertToProductResponseList()

        // then
        assertThat(actual).isEqualTo(
            mutableListOf(productResponse, productResponse)
        )
    }
}
