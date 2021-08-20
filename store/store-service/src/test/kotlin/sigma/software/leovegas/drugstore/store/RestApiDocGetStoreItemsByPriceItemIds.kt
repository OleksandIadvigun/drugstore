package sigma.software.leovegas.drugstore.store

import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.transaction.support.TransactionTemplate

@DisplayName("Get store items by price item ids REST API Doc test")
class RestApiDocGetStoreItemsByPriceItemIds @Autowired constructor(
    @LocalServerPort val port: Int,
    val storeProperties: StoreProperties,
    val storeRepository: StoreRepository,
    val transactionTemplate: TransactionTemplate
) : RestApiDocumentationTest() {

    @Test
    fun `should create order`() {

        // given
        transactionTemplate.execute {
            storeRepository.deleteAllInBatch()
        }

        // and
        val ids = transactionTemplate.execute {
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

        of("get-store-items-by-price-ids").`when`()
            .get("http://${storeProperties.host}:$port/api/v1/store/?ids=${ids?.get(0)}&ids=${ids?.get(1)}")
            .then()
            .assertThat().statusCode(200)
            .assertThat().body("size()", `is`(2))
            .assertThat().body("[0].priceItemId", equalTo(1))
            .assertThat().body("[1].priceItemId", equalTo(2))


    }
}
