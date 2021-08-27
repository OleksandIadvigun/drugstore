package sigma.software.leovegas.drugstore.accountancy

import java.math.BigDecimal
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import sigma.software.leovegas.drugstore.accountancy.api.PriceItemRequest
import sigma.software.leovegas.drugstore.accountancy.api.PriceItemResponse

@DisplayName("Price item converters test")
class PriceItemConvertersTest {

    @Test
    fun `should convert to PriceItemResponse`() {

        // given
        val priceItem = PriceItem(productId = 1L, price = BigDecimal.TEN, markup = BigDecimal.ZERO)
        val priceItemResponse = PriceItemResponse(productId = 1L, price = BigDecimal.TEN)

        // when
        val actual = priceItem.toPriceItemResponse()

        // then
        assertEquals(priceItemResponse, actual)
    }

    @Test
    fun `should convert to PriceItem`() {

        // given
        val priceItemRequest = PriceItemRequest(productId = 1L, price = BigDecimal.TEN, markup = BigDecimal.ZERO)
        val priceItem = PriceItem(productId = 1L, price = BigDecimal.TEN, markup = BigDecimal.ZERO)

        // when
        val actual = priceItemRequest.toEntity()

        // then
        assertEquals(priceItem, actual)
    }

    @Test
    fun `should convert list of priceItems to list of DTOs`() {

        // given
        val priceItem = PriceItem(productId = 1L, price = BigDecimal.TEN, markup = BigDecimal.ZERO)
        val priceItemResponse = PriceItemResponse(productId = 1L, price = BigDecimal.TEN)
        val priceItems = mutableListOf(priceItem, priceItem)

        // when
        val actual = priceItems.toPriceItemResponseList()

        // then
        assertThat(actual).isEqualTo(
            mutableListOf(priceItemResponse, priceItemResponse)
        )
    }
}
