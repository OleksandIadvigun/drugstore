package sigma.software.leovegas.drugstore.store

import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.Matchers
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.MediaType
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.store.api.UpdateStoreRequest

@DisplayName("Check store items quantity  REST API Doc test")
class RestApiDocCheckQuantityTest @Autowired constructor(
    val objectMapper: ObjectMapper,
    @LocalServerPort val port: Int,
    val storeProperties: StoreProperties,
    val storeRepository: StoreRepository,
    val transactionTemplate: TransactionTemplate
) : RestApiDocumentationTest(storeProperties) {

    @Test
    fun `should check store items quantity`() {

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
        val orderJson = objectMapper
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(
                listOf(
                    UpdateStoreRequest(
                        priceItemId = 1,
                        quantity = 5
                    )
                )
            )

        of("check-quantity")
            .`when`()
            .body(orderJson)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .put("http://${storeProperties.host}:$port/api/v1/store/check")
            .then()
            .assertThat().statusCode(202)
            .assertThat().body("size()", Matchers.`is`(1))
            .assertThat().body("[0].priceItemId", Matchers.equalTo(1))
            .assertThat().body("[0].quantity", Matchers.equalTo(10)) //
    }
}
