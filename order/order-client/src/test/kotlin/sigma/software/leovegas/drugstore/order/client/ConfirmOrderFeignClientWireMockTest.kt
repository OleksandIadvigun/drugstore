package sigma.software.leovegas.drugstore.order.client

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

@SpringBootApplication
internal class ConfirmOrderFeignClientWireMockTestApp

@DisplayName("Confirm Order Feign Client WireMock test")
@ContextConfiguration(classes = [ConfirmOrderFeignClientWireMockTestApp::class])
class ConfirmOrderFeignClientWireMockTest @Autowired constructor(
    val orderClient: OrderClient,
    val objectMapper: ObjectMapper,
) : WireMockTest() {

    @Test
    fun `should confirm order`() {

        // given
        val request = "1"

        // and
        stubFor(
            post("/api/v1/orders/confirm")
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
                                .writeValueAsString("Confirmed")
                        )
                        .withStatus(HttpStatus.CREATED.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                )
        )

        // when
        val responseActual = orderClient.confirmOrder(request)

        //  then
        assertThat(responseActual).isEqualTo("Confirmed")
    }
}
