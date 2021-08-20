package sigma.software.leovegas.drugstore.store.client

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
import sigma.software.leovegas.drugstore.store.api.CreateStoreRequest
import sigma.software.leovegas.drugstore.store.api.StoreResponse

@SpringBootApplication
internal class CreateStoreItemFeignClientWireMockTestApp

@DisplayName("Create Store Item Feign Client WireMock test")
@ContextConfiguration(classes = [CreateStoreItemFeignClientWireMockTestApp::class])
class CreateStoreItemFeignClientWireMockTest @Autowired constructor(
    val storeClient: StoreClient,
    val objectMapper: ObjectMapper,
) : WireMockTest() {

    @Test
    fun `should create store item`() {

        // given
        val request = CreateStoreRequest(
            priceItemId = 1,
            quantity = 10
        )

        // and
        val responseExpected = StoreResponse(
            id = 1L,
            priceItemId = 1,
            quantity = 10
        )

        // and
        stubFor(
            post("/api/v1/store")
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
        val responseActual = storeClient.createStoreItem(request)

        //  then
        assertThat(responseActual.id).isEqualTo(1L)
        assertThat(responseActual.priceItemId).isEqualTo(1)
        assertThat(responseActual.quantity).isEqualTo(10)
    }
}

