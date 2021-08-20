package sigma.software.leovegas.drugstore.store.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.put
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
import sigma.software.leovegas.drugstore.store.api.StoreResponse
import sigma.software.leovegas.drugstore.store.api.UpdateStoreRequest

@SpringBootApplication
internal class CheckQuantityFeignClientWireMockTestApp

@DisplayName("Check Store Item Quantity Feign Client WireMock test")
@ContextConfiguration(classes = [CheckQuantityFeignClientWireMockTestApp::class])
class CheckQuantityFeignClientWireMockTest @Autowired constructor(
    val storeClient: StoreClient,
    val objectMapper: ObjectMapper,
) : WireMockTest() {

    @Test
    fun `should check store items quantity`() {

        // given
        val request = listOf(
            UpdateStoreRequest(
                priceItemId = 1,
                quantity = 5
            )
        )

        // and
        val created = StoreResponse(
            id = 1,
            priceItemId = 1,
            quantity = 10
        )

        // and
        val responseExpected = listOf(
            StoreResponse(
                id = 1,
                priceItemId = 1,
                quantity = 10
            )
        )

        // and
        stubFor(
            put("/api/v1/store/check")
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
                        .withStatus(HttpStatus.ACCEPTED.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                )
        )

        // when
        val responseActual = storeClient.checkAvailability(request)

        //  then
        assertThat(responseActual).hasSize(1)
        assertThat(responseActual[0].id).isEqualTo(1)
        assertThat(responseActual[0].priceItemId).isEqualTo(1)
        assertThat(responseActual[0].quantity).isEqualTo(10)
    }
}
