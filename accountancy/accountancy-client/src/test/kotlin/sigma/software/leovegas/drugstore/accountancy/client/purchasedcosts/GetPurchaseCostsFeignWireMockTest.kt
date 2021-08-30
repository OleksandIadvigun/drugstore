package sigma.software.leovegas.drugstore.accountancy.client.purchasedcosts

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.matching.ContainsPattern
import java.time.LocalDateTime
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import sigma.software.leovegas.drugstore.accountancy.api.PurchasedCostsResponse
import sigma.software.leovegas.drugstore.accountancy.client.AccountancyClient
import sigma.software.leovegas.drugstore.accountancy.client.WireMockTest

@SpringBootApplication
internal class GetPurchaseCostsFeignWireMockTestApp

@DisplayName("Get Purchased Costs Feign Client WireMock test")
@ContextConfiguration(classes = [GetPurchaseCostsFeignWireMockTestApp::class])
class GetPurchaseCostsFeignWireMockTest @Autowired constructor(
    val accountancyClient: AccountancyClient,
    val objectMapper: ObjectMapper
) : WireMockTest() {

    @Test
    fun `should get purchased costs`() {

        // given
        val responseExpected = listOf(
            PurchasedCostsResponse(
                id = 1,
                priceItemId = 1,
                quantity = 10,
            )
        )

        // and
        stubFor(
            get("/api/v1/accountancy/purchased-costs")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(
                    aResponse()
                        .withBody(
                            objectMapper
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(responseExpected)
                        )
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                )
        )

        // when
        val responseActual = accountancyClient.getPurchasedCosts()

        // then
        assertThat(responseActual).hasSize(1)
        assertThat(responseActual[0].id).isEqualTo(responseExpected[0].id)
    }

    @Test
    fun `should get purchased costs before date`() {

        // given
        val responseExpected = listOf(
            PurchasedCostsResponse(
                id = 1,
                priceItemId = 1,
                quantity = 10,
            )
        )

        // and
        val dateTo = LocalDateTime.now().plusDays(1)

        // and
        stubFor(
            get("/api/v1/accountancy/purchased-costs?dateTo=${dateTo}")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(
                    aResponse()
                        .withBody(
                            objectMapper
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(responseExpected)
                        )
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                )
        )

        // when
        val responseActual = accountancyClient.getPurchasedCosts()

        // then
        assertThat(responseActual).hasSize(1)
        assertThat(responseActual[0].id).isEqualTo(responseExpected[0].id)
    }

    @Test
    fun `should get purchased costs after date`() {

        // given
        val responseExpected = listOf(
            PurchasedCostsResponse(
                id = 1,
                priceItemId = 1,
                quantity = 10,
            )
        )

        // and
        val dateFrom = LocalDateTime.now().minusDays(1)

        // and
        stubFor(
            get("/api/v1/accountancy/purchased-costs?dateFrom=${dateFrom}")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(
                    aResponse()
                        .withBody(
                            objectMapper
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(responseExpected)
                        )
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                )
        )

        // when
        val responseActual = accountancyClient.getPurchasedCosts()

        // then
        assertThat(responseActual).hasSize(1)
        assertThat(responseActual[0].id).isEqualTo(responseExpected[0].id)
    }

    @Test
    fun `should get purchased costs between date`() {

        // given
        val responseExpected = listOf(
            PurchasedCostsResponse(
                id = 1,
                priceItemId = 1,
                quantity = 10,
            )
        )

        // and
        val dateFrom = LocalDateTime.now().minusDays(1)
        val dateTo = LocalDateTime.now().plusDays(1)

        // and
        stubFor(
            get("/api/v1/accountancy/purchased-costs?dateFrom=${dateFrom}&dateTo=${dateTo}")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(
                    aResponse()
                        .withBody(
                            objectMapper
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(responseExpected)
                        )
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                )
        )

        // when
        val responseActual = accountancyClient.getPurchasedCosts()

        // then
        assertThat(responseActual).hasSize(1)
    }
}
