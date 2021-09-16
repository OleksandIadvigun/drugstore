package sigma.software.leovegas.drugstore.store.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
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
import sigma.software.leovegas.drugstore.store.api.CheckStatusResponse


@SpringBootApplication
internal class CheckTransferFeignClientWireMockTestApp

@DisplayName("Check Transfer of products Feign Client WireMock test")
@ContextConfiguration(classes = [CheckTransferFeignClientWireMockTestApp::class])
class CheckTransferFeignClientWireMockTest @Autowired constructor(
    val storeClient: StoreClient,
    val objectMapper: ObjectMapper,
) : WireMockTest() {

    @Test
    fun `should check transfer of products`() {

        // given
        val orderNumber = "1"

        // and
        stubFor(
            WireMock.get("/api/v1/store/check-transfer/$orderNumber")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(
                    aResponse()
                        .withBody(
                            objectMapper
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(CheckStatusResponse(orderNumber))
                        )
                        .withStatus(HttpStatus.ACCEPTED.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                )
        )

        // when
        val responseActual = storeClient.checkTransfer(orderNumber)

        //  then
        assertThat(responseActual.orderNumber).isEqualTo(orderNumber)
    }
}
