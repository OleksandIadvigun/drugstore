package sigma.software.leovegas.drugstore.store

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.support.TransactionTemplate
import org.assertj.core.api.Assertions.assertThat

@DisplayName("Store Repository test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class StoreRepositoryTest(
    @Autowired val transactionalTemplate: TransactionTemplate,
    @Autowired val storeRepository: StoreRepository
) {

    @Test
    fun `should get store items`() {

        // given
        transactionalTemplate.execute{
            storeRepository.deleteAll()
        }

        // and
        val created = transactionalTemplate.execute {
            storeRepository.save(
                Store(
                    priceItemId = 1,
                    quantity = 5
                )
            )
        } ?: fail("result expected")

        // when
        val found = transactionalTemplate.execute {
            storeRepository.getStoreByPriceItemIds(listOf(created.priceItemId))
        } ?: fail("result expected")

        // then
        assertThat(found[0].id).isEqualTo(created.id)
    }
}
