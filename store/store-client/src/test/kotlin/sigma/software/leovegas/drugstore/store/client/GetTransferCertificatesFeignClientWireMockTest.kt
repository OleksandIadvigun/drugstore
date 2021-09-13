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
import sigma.software.leovegas.drugstore.store.api.TransferCertificateResponse
import sigma.software.leovegas.drugstore.store.api.TransferStatusDTO

@SpringBootApplication
internal class GetTransferCertificatesFeignClientWireMockTestApp

@DisplayName("Get Transfer Certificated by invoice id Feign Client WireMock test")
@ContextConfiguration(classes = [GetTransferCertificatesFeignClientWireMockTestApp::class])
class GetTransferCertificatesFeignClientWireMockTest @Autowired constructor(
    val storeClient: StoreClient,
    val objectMapper: ObjectMapper,
) : WireMockTest() {

    @Test
    fun `should get transfer certificate`() {

        // given
        val responseExpected = listOf(
            TransferCertificateResponse(
                certificateNumber = 1,
                orderNumber = 1,
                status = TransferStatusDTO.DELIVERED
            ),
            TransferCertificateResponse(
                certificateNumber = 2,
                orderNumber = 2,
                status = TransferStatusDTO.RECEIVED
            )
        )

        // and
        stubFor(
            get("/api/v1/store/transfer-certificate")
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
        val responseActual = storeClient.getTransferCertificates()

        //  then
        assertThat(responseActual).hasSize(2)
    }
}
