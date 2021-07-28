package sigma.software.leovegas.drugstore.product

import java.math.BigDecimal
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ProductConvertersTest {
    //given
    private val product = Product(name = "test", price = BigDecimal.TEN,quantity = 0)
    private val productResponse = ProductResponse(name = "test", price = BigDecimal.TEN,quantity = 0)
    private val productRequest = ProductRequest(name = "test", price = BigDecimal.TEN,quantity = 0)

    @Test
    fun `should convert to ProductResponse`() {
        //when
        val actual = product.convertToProductResponse()

        //then
        assertEquals(productResponse, actual)
    }

    @Test
    fun `should convert to Product`() {
        //when
        val actual = productRequest.convertToProduct()

        //then
        assertEquals(product, actual)
    }

    @Test
    fun `should convert list of products to list of DTOs`() {
        // given
        val products = mutableListOf(product, product)

        // when
        val actual = products.convertToProductResponseList()

        // then
        assertThat(actual).isEqualTo(
            mutableListOf(productResponse, productResponse)
        )
    }
}
