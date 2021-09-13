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
import sigma.software.leovegas.drugstore.store.api.TransferCertificateResponse
import sigma.software.leovegas.drugstore.store.api.TransferStatusDTO

@SpringBootApplication
internal class DeliverProductsFeignClientWireMockTestApp

@DisplayName("Deliver Products Feign Client WireMock test")
@ContextConfiguration(classes = [DeliverProductsFeignClientWireMockTestApp::class])
class DeliverProductsFeignClientWireMockTest @Autowired constructor(
    val storeClient: StoreClient,
    val objectMapper: ObjectMapper,
) : WireMockTest() {

    @Test
    fun `should deliver products`() {

        // given
        val orderNumber: Long = 1

        // given
        val request = 1L

        // and
        val responseExpected = TransferCertificateResponse(
            certificateNumber = 1,
            orderNumber = 1,
            status = TransferStatusDTO.DELIVERED
        )

        // and
        stubFor(
            put("/api/v1/store/deliver")
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
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                )
        )

        // when
        val responseActual = storeClient.deliverProducts(orderNumber)

        //  then
        assertThat(responseActual.certificateNumber).isEqualTo(orderNumber)
        assertThat(responseActual.status).isEqualTo(TransferStatusDTO.DELIVERED)
    }
}
