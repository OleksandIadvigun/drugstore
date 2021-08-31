package sigma.software.leovegas.drugstore.accountancy.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.matching.ContainsPattern
import java.math.BigDecimal
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import sigma.software.leovegas.drugstore.accountancy.api.PurchasedItemDTO

@SpringBootApplication
internal class GetPastPurchasedItemsFeignClientWireMockTestApp

@DisplayName("Get Purchased Items Feign Client WireMock test")
@ContextConfiguration(classes = [GetPastPurchasedItemsFeignClientWireMockTestApp::class])
class GetPastPurchasedItemsFeignClientWireMockTest @Autowired constructor(
    val accountancyClient: AccountancyClient,
    val objectMapper: ObjectMapper
) : WireMockTest() {

    @Test
    fun `should get purchased items`() {

        // given
        val responseExpected = listOf(
            PurchasedItemDTO(
                name = "test1",
                price = BigDecimal.TEN,
                quantity = 10
            ),
            PurchasedItemDTO(
                name = "test1",
                price = BigDecimal.ONE,
                quantity = 2
            )
        )

        //and
        stubFor(
            get("/api/v1/accountancy/past-purchased-items")
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
        val responseActual = accountancyClient.getPastPurchasedItems()

        assertThat(responseActual).hasSize(2)
        assertThat(responseActual[0].quantity).isEqualTo(10)
        assertThat(responseActual[1].quantity).isEqualTo(2)
    }
}
