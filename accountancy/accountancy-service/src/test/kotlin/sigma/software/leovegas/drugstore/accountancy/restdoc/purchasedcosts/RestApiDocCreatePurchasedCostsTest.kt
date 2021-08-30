package sigma.software.leovegas.drugstore.accountancy.restdoc.purchasedcosts

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.put
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import com.github.tomakehurst.wiremock.matching.ContainsPattern
import com.github.tomakehurst.wiremock.matching.EqualToPattern
import java.math.BigDecimal
import org.assertj.core.api.Assertions.fail
import org.hamcrest.Matchers.emptyString
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.accountancy.PriceItem
import sigma.software.leovegas.drugstore.accountancy.PriceItemRepository
import sigma.software.leovegas.drugstore.accountancy.PurchasedCostsRepository
import sigma.software.leovegas.drugstore.accountancy.api.PurchasedCostsCreateRequest
import sigma.software.leovegas.drugstore.accountancy.client.AccountancyProperties
import sigma.software.leovegas.drugstore.accountancy.restdoc.RestApiDocumentationTest
import sigma.software.leovegas.drugstore.store.api.StoreResponse
import sigma.software.leovegas.drugstore.store.api.UpdateStoreRequest

@DisplayName("Create purchased costs REST API Doc test")
class RestApiDocCreatePurchasedCostsTest @Autowired constructor(
    val objectMapper: ObjectMapper,
    @LocalServerPort val port: Int,
    val accountancyProperties: AccountancyProperties,
    val transactionTemplate: TransactionTemplate,
    val priceItemRepository: PriceItemRepository,
    val purchasedCostsRepository: PurchasedCostsRepository
) : RestApiDocumentationTest(accountancyProperties) {

    @Test
    fun `should create purchased costs`() {

        // setup
        val wireMockServerStoreClient = WireMockServer(wireMockConfig().port(8083))
        wireMockServerStoreClient.start()

        // given
        transactionTemplate.execute {
            priceItemRepository.deleteAll()
        }

        // and
        transactionTemplate.execute {
            purchasedCostsRepository.deleteAll()
        }

        // and
        val priceItem = transactionTemplate.execute {
            priceItemRepository.save(
                PriceItem(
                    productId = 1L,
                    price = BigDecimal("25.50"),
                    markup = BigDecimal.ZERO
                )
            )
        } ?: fail("result is expected")

        // and
        val storeIncreaseResponse = listOf(
            StoreResponse(
                id = 1,
                priceItemId = priceItem.id ?: -1,
                quantity = 12
            )
        )

        // and
        val storeGetResponse = listOf(
            StoreResponse(
                id = 1,
                priceItemId = priceItem.id ?: -1,
                quantity = 2
            )
        )

        // and
        wireMockServerStoreClient.stubFor(
            get("/api/v1/store/price-ids/?ids=${priceItem.id}")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(
                    aResponse()
                        .withBody(
                            objectMapper
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(storeGetResponse)
                        )
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                )
        )

        // and
        wireMockServerStoreClient.stubFor(
            put("/api/v1/store/increase")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
                .withRequestBody(
                    EqualToPattern(
                        objectMapper
                            .writerWithDefaultPrettyPrinter()
                            .writeValueAsString(
                                listOf(
                                    UpdateStoreRequest(priceItem.id ?: -1, 10)
                                )
                            )
                    )
                )
                .willReturn(
                    aResponse()
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
            .writeValueAsString(
                PurchasedCostsCreateRequest(
                    priceItemId = priceItem.id ?: -1,
                    quantity = 10,
                )
            )

        of("create-purchased-costs").`when`()
            .body(priceItemJson)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .post("http://${accountancyProperties.host}:$port/api/v1/accountancy/purchased-costs")
            .then()
            .assertThat().statusCode(201)
            .assertThat().body("id", notNullValue())
            .assertThat().body("quantity", equalTo(10))
            .assertThat().body("dateOfPurchase", not(emptyString()))

        wireMockServerStoreClient.stop()
    }
}
