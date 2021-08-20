package sigma.software.leovegas.drugstore.store

import org.hamcrest.Matchers
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.transaction.support.TransactionTemplate

@DisplayName("Get store items REST API Doc test")
class RestApiDocGetStoreItemsTest @Autowired constructor(
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

        of("get-store-items").`when`()
            .get("http://${storeProperties.host}:$port/api/v1/store")
            .then()
            .assertThat().statusCode(200)
            .assertThat().body("size()", Matchers.`is`(2))
    }
}
