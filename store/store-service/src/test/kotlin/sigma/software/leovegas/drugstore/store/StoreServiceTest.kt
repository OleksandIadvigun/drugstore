package sigma.software.leovegas.drugstore.store

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.fail
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.store.api.CreateStoreRequest
import sigma.software.leovegas.drugstore.store.api.UpdateStoreRequest

@SpringBootTest
@AutoConfigureTestDatabase
@DisplayName("Store Service test")
class StoreServiceTest @Autowired constructor(
    val storeRepository: StoreRepository,
    val transactionTemplate: TransactionTemplate,
    val storeService: StoreService,
) {

    @Test
    fun `should create store`() {

        //given
        transactionTemplate.execute {
            storeRepository.deleteAllInBatch()
        }

        // given
        val storeRequest = CreateStoreRequest(
            priceItemId = 1,
            quantity = 10
        )

        // when
        val created = transactionTemplate.execute {
            storeService.createStoreItem(storeRequest)
        } ?: fail("result expected")

        // then
        assertThat(created.id).isNotNull
        assertThat(created.priceItemId).isEqualTo(1)
        assertThat(created.quantity).isEqualTo(10)

    }

    @Test
    fun `should not create store with the same price item `() {

        // given
        val storeRequest = CreateStoreRequest(
            priceItemId = 1,
            quantity = 10
        )

        // and
        val created = transactionTemplate.execute {
            storeRepository.save(
                Store(
                    priceItemId = 1,
                    quantity = 5
                )
            )
        } ?: fail("result expected")

        // when
        val exception = assertThrows<StoreItemWithThisPriceItemAlreadyExistException> {
            storeService.createStoreItem(storeRequest)
        }

        // then
        assertThat(exception.message).contains("Store with this price item", "already exist!")
    }

    @Test
    fun `should get store items by price item ids`() {

        //given
        transactionTemplate.execute {
            storeRepository.deleteAllInBatch()
        }

        // given
        val created = transactionTemplate.execute {
            storeRepository.saveAll(
                listOf(
                    Store(
                        priceItemId = 1,
                        quantity = 10
                    ), Store(
                        priceItemId = 2,
                        quantity = 5
                    )
                )
            )
        }?.map { it.priceItemId }

        // when
        val actual = transactionTemplate.execute {
            storeService.getStoreItemsByPriceItemIds(created as List<Long>)
        }

        // then
        assertThat(actual).hasSize(2)
        assertThat(actual?.get(0)?.priceItemId ?: -1).isEqualTo(created?.get(0) ?: -1)
        assertThat(actual?.get(1)?.priceItemId ?: -1).isEqualTo(created?.get(1) ?: -1)
    }

    @Test
    fun `should get store items`() {

        //given
        transactionTemplate.execute {
            storeRepository.deleteAllInBatch()
        }

        // given
        val created = transactionTemplate.execute {
            storeRepository.saveAll(
                listOf(
                    Store(
                        priceItemId = 1,
                        quantity = 10
                    ), Store(
                        priceItemId = 2,
                        quantity = 5
                    )
                )
            )
        }

        // when
        val actual = transactionTemplate.execute {
            storeService.getStoreItems()
        }

        // then
        assertThat(actual).hasSize(2)
        assertThat(actual?.get(0)?.id ?: -1).isEqualTo(created?.get(0)?.id ?: -1)
        assertThat(actual?.get(1)?.id ?: -1).isEqualTo(created?.get(1)?.id ?: -1)
    }

    @Test
    fun `should increase store item quantity`() {

        //given
        transactionTemplate.execute {
            storeRepository.deleteAllInBatch()
        }

        // and
        val created = transactionTemplate.execute {
            storeRepository.save(
                Store(
                    priceItemId = 1,
                    quantity = 10
                )
            )
        }

        // and
        val request = listOf(
            UpdateStoreRequest(
                priceItemId = 1,
                quantity = 3
            )
        )

        // when
        val actual = storeService.increaseQuantity(request)

        // then
        assertThat(actual[0].quantity).isEqualTo(13) // 10+3=7
    }

    @Test
    fun `should check store item quantity`() {

        //given
        transactionTemplate.execute {
            storeRepository.deleteAll()
        }

        // and
        val created = transactionTemplate.execute {
            storeRepository.save(
                Store(
                    priceItemId = 1,
                    quantity = 10
                )
            )
        }

        // and
        val request = listOf(
            UpdateStoreRequest(
                priceItemId = 1,
                quantity = 3
            )
        )

        // when
        val result = storeService.checkAvailability(request)

        //then
        assertThat(result).hasSize(1)
        assertThat(result[0].priceItemId).isEqualTo(1)
        assertThat(result[0].quantity).isEqualTo(10)
    }

    @Test
    fun `should not get true for not enough amount of store item`() {

        //given
        transactionTemplate.execute {
            storeRepository.deleteAll()
        }

        // and
        val created = transactionTemplate.execute {
            storeRepository.save(
                Store(
                    priceItemId = 1,
                    quantity = 1
                )
            )
        }

        // and
        val request = listOf(
            UpdateStoreRequest(
                priceItemId = 1,
                quantity = 5
            )
        )

        // when
        val exception = assertThrows<InsufficientAmountOfStoreItemException> {
            storeService.checkAvailability(request)
        }

        // then
        assertThat(exception.message).contains("Insufficient amount of store with price item id")
    }

    @Test
    fun `should reduce store item quantity`() {

        //given
        transactionTemplate.execute {
            storeRepository.deleteAllInBatch()
        }

        // and
        val created = transactionTemplate.execute {
            storeRepository.save(
                Store(
                    priceItemId = 1,
                    quantity = 10
                )
            )
        }

        // and
        val request = listOf(
            UpdateStoreRequest(
                priceItemId = 1,
                quantity = 3
            )
        )

        // when
        val actual = storeService.reduceQuantity(request)

        // then
        assertThat(actual[0].quantity).isEqualTo(7) // 10-3=7
    }
}
