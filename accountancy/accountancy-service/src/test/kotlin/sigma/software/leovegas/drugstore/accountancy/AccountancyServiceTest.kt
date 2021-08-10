package sigma.software.leovegas.drugstore.accountancy

import java.math.BigDecimal
import java.time.LocalDateTime
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
import sigma.software.leovegas.drugstore.accountancy.api.PriceItemRequest
import sigma.software.leovegas.drugstore.accountancy.api.PriceItemResponse

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
@DisplayName("Accountancy Service test")
class AccountancyServiceTest @Autowired constructor(
    val service: AccountancyService,
    val transactionTemplate: TransactionTemplate,
    val priceItemRepository: PriceItemRepository
) {

    @Test
    fun `should create price item`() {

        // given
        val priceItem = PriceItemRequest(
            productId = 1L,
            price = BigDecimal("25.50"),
        )

        //and
        val priceItemResponse = PriceItemResponse(
            productId = 1L,
            price = BigDecimal("25.50"),
        )

        // when
        val actual = service.createPriceItem(priceItem)

        // then
        assertNotNull(actual)
        assertNotNull(actual.id)
        assertEquals(priceItemResponse.productId, actual.productId)
        assertEquals(priceItemResponse.price, actual.price)
        assertThat(actual.createdAt).isBefore(LocalDateTime.now())
    }

    @Test
    fun `should update product`() {

        // given
        val priceItem = PriceItemRequest(
            productId = 1L,
            price = BigDecimal("25.50"),
        )

        // and
        val saved = transactionTemplate.execute {
            priceItemRepository.save(priceItem.toEntity()).toPriceItemResponse()
        } ?: fail("result is expected")

        // and
        val updatedProductRequest = PriceItemRequest(
            productId = 1L,
            price = BigDecimal("35.50"),
        )

        // when
        val actual = service.updatePriceItem(saved.id, updatedProductRequest)

        // then
        assertNotNull(actual)
        assertEquals(updatedProductRequest.price, actual.price)
        assertThat(actual.createdAt).isBefore(LocalDateTime.now())   // todo should work before updated
    }

    @Test
    fun `should not update not existing product`() {

        // given
        val id = Long.MAX_VALUE

        // and
        val priceItemRequest = PriceItemRequest(
            productId = 1L,
            price = BigDecimal("25.50"),
        )

        // when
        val exception = assertThrows<ResourceNotFoundException> {
            service.updatePriceItem(Long.MAX_VALUE, priceItemRequest)
        }

        // then
        assertThat(exception.message).isEqualTo("This price item with id: $id doesn't exist!")
    }
}
