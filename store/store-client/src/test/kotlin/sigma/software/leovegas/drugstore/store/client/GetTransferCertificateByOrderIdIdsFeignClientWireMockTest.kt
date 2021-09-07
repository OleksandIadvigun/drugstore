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
internal class GetTransferCertificateByOrderIdIdsFeignClientWireMockTestApp

@DisplayName("Get Transfer Certificated by order id Feign Client WireMock test")
@ContextConfiguration(classes = [GetTransferCertificateByOrderIdIdsFeignClientWireMockTestApp::class])
class GetTransferCertificateByOrderIdIdsFeignClientWireMockTest @Autowired constructor(
    val storeClient: StoreClient,
    val objectMapper: ObjectMapper,
) : WireMockTest() {

    @Test
    fun `should get transfer certificate by order id`() {

        //given
        val orderNumber: Long = 1

        // and
        val responseExpected =
            TransferCertificateResponse(
                id = 1,
                orderId = orderNumber,
                status = TransferStatusDTO.DELIVERED
            )

        // and
        stubFor(
            get("/api/v1/store/transfer-certificate/order/$orderNumber")
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
        val responseActual = storeClient.getTransferCertificatesByOrderId(orderNumber)

        //  then
        assertThat(responseActual.orderId).isEqualTo(orderNumber)
        assertThat(responseActual.status).isEqualTo(TransferStatusDTO.DELIVERED)
    }
}
