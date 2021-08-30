package sigma.software.leovegas.drugstore.accountancy.client.purchasedcosts

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.matching.ContainsPattern
import com.github.tomakehurst.wiremock.matching.EqualToPattern
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import sigma.software.leovegas.drugstore.accountancy.api.PurchasedCostsCreateRequest
import sigma.software.leovegas.drugstore.accountancy.api.PurchasedCostsResponse
import sigma.software.leovegas.drugstore.accountancy.client.AccountancyClient
import sigma.software.leovegas.drugstore.accountancy.client.WireMockTest

@SpringBootApplication
internal class CreatePurchasedCostsFeignClientWireMockTestApp

@DisplayName("Create Purchased Costs Feign Client WireMock test")
@ContextConfiguration(classes = [CreatePurchasedCostsFeignClientWireMockTestApp::class])
class CreatePurchasedCostsFeignClientWireMockTest @Autowired constructor(
    val accountancyClient: AccountancyClient,
    val objectMapper: ObjectMapper
) : WireMockTest() {

    @Test
    fun `should create purchased costs`() {

        // given
        val request = PurchasedCostsCreateRequest(
            priceItemId = 1,
            quantity = 10,
        )

        // and
        val responseExpected = PurchasedCostsResponse(
            id = 1,
            priceItemId = 1,
            quantity = 10,
        )

        // and
        stubFor(
            post("/api/v1/accountancy/purchased-costs")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
                .withRequestBody(
                    EqualToPattern(
                        objectMapper
                            .writerWithDefaultPrettyPrinter()
                            .writeValueAsString(request)
                    )
                )
                .willReturn(
                    aResponse()
                        .withBody(
                            objectMapper
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(responseExpected)
                        )
                        .withStatus(HttpStatus.CREATED.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                )
        )

        // when
        val responseActual = accountancyClient.createPurchasedCosts(request)

        //  then
        assertThat(responseActual.id).isEqualTo(1)
        assertThat(responseActual.priceItemId).isEqualTo(1)
        assertThat(responseActual.quantity).isEqualTo(10)
    }
}
