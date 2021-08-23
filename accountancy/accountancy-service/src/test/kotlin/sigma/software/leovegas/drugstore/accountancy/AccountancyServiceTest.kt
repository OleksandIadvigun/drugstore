package sigma.software.leovegas.drugstore.accountancy

import java.math.BigDecimal
import java.time.LocalDateTime
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
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
        assertThat(actual).isNotNull
        assertThat(actual.id).isNotNull
        assertEquals(priceItemResponse.productId, actual.productId)
        assertEquals(priceItemResponse.price, actual.price)
        assertThat(actual.createdAt).isBefore(LocalDateTime.now())
    }

    @Test
    fun `should update price item`() {

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
        assertThat(actual).isNotNull
        assertThat(updatedProductRequest.price).isEqualTo(actual.price)
        assertThat(actual.createdAt).isBefore(actual.updatedAt)
    }

    @Test
    fun `should not update not existing price item`() {

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

    @Test
    fun `should get products price`() {

        // given
        val priceItem = PriceItemRequest(
            productId = 1L,
            price = BigDecimal("25.50"),
        )

        // and
        val saved = transactionTemplate.execute {
            priceItemRepository.save(priceItem.toEntity()).toPriceItemResponse()
        } ?: fail("result is expected")

        // when
        val actual = service.getProductsPrice()

        // then
        assertThat(actual).isNotNull
        assertThat(saved.price).isEqualTo(actual[1])
    }

    @Test
    fun `should get products price by products ids`() {

        // given
        val saved = transactionTemplate.execute {
            priceItemRepository.saveAll(
                listOf(
                    PriceItem(
                        productId = 1L,
                        price = BigDecimal("25.50")
                    ),
                    PriceItem(
                        productId = 2L,
                        price = BigDecimal("35.50")
                    ),
                )
            )
        } ?: fail("result is expected")

        // when
        val actual = service.getProductsPriceByProductIds(listOf(1L, 2L))

        // then
        assertThat(actual).isNotNull
        assertThat(actual.size).isEqualTo(2)
        assertThat(saved[0].price).isEqualTo(actual[1])
        assertThat(saved[1].price).isEqualTo(actual[2])
    }

    @Test
    fun `should get price items by ids`() {

        // given
        transactionTemplate.execute {
            priceItemRepository.deleteAllInBatch()
        }

        // given
        val saved = transactionTemplate.execute {
            priceItemRepository.saveAll(
                listOf(
                    PriceItem(
                        productId = 1L,
                        price = BigDecimal("25.50")
                    ),
                    PriceItem(
                        productId = 2L,
                        price = BigDecimal("35.50")
                    ),
                    PriceItem(
                        productId = 3L,
                        price = BigDecimal("45.50")
                    )
                )
            )
        } ?: fail("result is expected")

        val ids = saved.map { it.id }

        // when
        val actual = service.getPriceItemsByIds(ids as List<Long>)

        // then
        assertThat(actual).isNotNull
        assertThat(actual.size).isEqualTo(3)
        assertThat(actual[0].price).isEqualTo(BigDecimal("25.50"))
        assertThat(actual[1].price).isEqualTo(BigDecimal("35.50"))
        assertThat(actual[2].price).isEqualTo(BigDecimal("45.50"))
    }
}
