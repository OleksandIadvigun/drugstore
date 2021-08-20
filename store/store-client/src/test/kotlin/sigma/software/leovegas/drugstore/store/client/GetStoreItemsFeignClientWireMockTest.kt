package sigma.software.leovegas.drugstore.store.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.matching.ContainsPattern
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import sigma.software.leovegas.drugstore.store.api.StoreResponse

@SpringBootApplication
internal class GetStoreItemsFeignClientWireMockTestApp

@DisplayName("Get Store Items Feign Client WireMock test")
@ContextConfiguration(classes = [GetStoreItemsFeignClientWireMockTestApp::class])
class GetStoreItemsFeignClientWireMockTest @Autowired constructor(
    val storeClient: StoreClient,
    val objectMapper: ObjectMapper,
) : WireMockTest() {

    @Test
    fun `should get store items`() {

        // given
        val responseExpected = listOf(
            StoreResponse(
                id = 1,
                priceItemId = 1,
                quantity = 10
            ), StoreResponse(
                id = 2,
                priceItemId = 2,
                quantity = 5
            )
        )

        // and
        stubFor(
            get("/api/v1/store")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(
                    aResponse()
                        .withBody(
                            objectMapper
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(responseExpected)
                        )
                        .withStatus(HttpStatus.OK.value())
                )
        )

        // when
        val responseActual = storeClient.getStoreItems()

        //  then
        assertThat(responseActual).hasSize(2)
    }
}
