package sigma.software.leovegas.drugstore.accountancy.restdoc.purchasedcosts

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import com.github.tomakehurst.wiremock.matching.ContainsPattern
import com.github.tomakehurst.wiremock.matching.EqualToPattern
import org.assertj.core.api.Assertions.fail
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.accountancy.PurchasedCosts
import sigma.software.leovegas.drugstore.accountancy.PurchasedCostsRepository
import sigma.software.leovegas.drugstore.accountancy.api.PurchasedCostsUpdateRequest
import sigma.software.leovegas.drugstore.accountancy.client.AccountancyProperties
import sigma.software.leovegas.drugstore.accountancy.restdoc.RestApiDocumentationTest
import sigma.software.leovegas.drugstore.store.api.StoreResponse
import sigma.software.leovegas.drugstore.store.api.UpdateStoreRequest

@DisplayName("Update purchased costs REST API Doc test")
class RestApiDocUpdatePurchasedCostsTest @Autowired constructor(
    val objectMapper: ObjectMapper,
    @LocalServerPort val port: Int,
    val accountancyProperties: AccountancyProperties,
    val transactionTemplate: TransactionTemplate,
    val purchasedCostsRepository: PurchasedCostsRepository
) : RestApiDocumentationTest(accountancyProperties) {

    @Test
    fun `should update purchased costs`() {

        // setup
        val wireMockServerStoreClient = WireMockServer(wireMockConfig().port(8083))
        wireMockServerStoreClient.start()

        // given
        transactionTemplate.execute {
            purchasedCostsRepository.deleteAll()
        }

        // and
        val purchasedCosts = transactionTemplate.execute {
            purchasedCostsRepository.save(
                PurchasedCosts(
                    priceItemId = 1,
                    quantity = 5,
                )
            )
        } ?: fail("result is expected")

        // and
        val storeReduceResponse = listOf(
            StoreResponse(
                id = 1,
                priceItemId = purchasedCosts.priceItemId,
                quantity = 5
            )
        )

        // and
        val storeIncreaseResponse = listOf(
            StoreResponse(
                id = 1,
                priceItemId = purchasedCosts.priceItemId,
                quantity = 10
            )
        )

        // and
        wireMockServerStoreClient.stubFor(
            WireMock.put("/api/v1/store/reduce")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
                .withRequestBody(
                    EqualToPattern(
                        objectMapper
                            .writerWithDefaultPrettyPrinter()
                            .writeValueAsString(
                                listOf(
                                    UpdateStoreRequest(
                                        purchasedCosts.priceItemId, purchasedCosts.quantity
                                    )
                                )
                            )
                    )
                )
                .willReturn(
                    WireMock.aResponse()
                        .withBody(
                            objectMapper
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(storeReduceResponse)
                        )
                        .withStatus(HttpStatus.OK.value())
                )
        )

        // and
        wireMockServerStoreClient.stubFor(
            WireMock.put("/api/v1/store/increase")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
                .withRequestBody(
                    EqualToPattern(
                        objectMapper
                            .writerWithDefaultPrettyPrinter()
                            .writeValueAsString(
                                listOf(
                                    UpdateStoreRequest(
                                        purchasedCosts.priceItemId, 10
                                    )
                                )
                            )
                    )
                )
                .willReturn(
                    WireMock.aResponse()
                        .withBody(
                            objectMapper
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(storeIncreaseResponse)
                        )
                        .withStatus(HttpStatus.OK.value())
                )
        )

        // and
        val priceItemJson = objectMapper
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(PurchasedCostsUpdateRequest(10))

        of("update-purchased-costs").`when`()
            .pathParam("id", purchasedCosts.id)
            .body(priceItemJson)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .put("http://${accountancyProperties.host}:$port/api/v1/accountancy/purchased-costs/{id}")
            .then()
            .assertThat().statusCode(202)
            .assertThat().body("id", notNullValue())
            .assertThat().body("quantity", equalTo(10))

        wireMockServerStoreClient.stop()
    }
}
