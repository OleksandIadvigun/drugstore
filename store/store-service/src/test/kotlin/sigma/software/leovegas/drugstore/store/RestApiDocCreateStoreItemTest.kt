package sigma.software.leovegas.drugstore.store

import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.Matchers
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.MediaType
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.store.api.CreateStoreRequest

@DisplayName("Create store item REST API Doc test")
class RestApiDocCreateStoreItemTest @Autowired constructor(
    val objectMapper: ObjectMapper,
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
        val storeJson = objectMapper
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(
                CreateStoreRequest(
                    priceItemId = 1,
                    quantity = 10
                )
            )

        of("create-store-item").`when`()
            .body(storeJson)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .post("http://${storeProperties.host}:$port/api/v1/store")
            .then()
            .assertThat().statusCode(201)
            .assertThat().body("priceItemId", Matchers.equalTo(1))
            .assertThat().body("quantity", Matchers.equalTo(10))
    }
}
