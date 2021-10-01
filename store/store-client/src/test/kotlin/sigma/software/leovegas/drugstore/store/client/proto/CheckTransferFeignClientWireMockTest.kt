package sigma.software.leovegas.drugstore.store.client.proto

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.http.HttpStatus
import org.springframework.test.context.ContextConfiguration
import sigma.software.leovegas.drugstore.api.protobuf.Proto
import sigma.software.leovegas.drugstore.store.client.WireMockTest


@SpringBootApplication
internal class CheckTransferFeignClientWireMockTestApp

@DisplayName("Check Transfer of products Feign Client WireMock test")
@ContextConfiguration(classes = [CheckTransferFeignClientWireMockTestApp::class])
class CheckTransferFeignClientWireMockTest @Autowired constructor(
    val storeClientProto: StoreClientProto,
    val objectMapper: ObjectMapper,
) : WireMockTest() {

    @Test
    fun `should check transfer of products`() {

        // given
        val orderNumber = "1"

        val responseExpected =
            Proto.CheckTransferResponse.newBuilder().setOrderNumber(orderNumber).setComment("Not delivered").build()

        // and
        stubFor(
            WireMock.get("/api/v1/store/check-transfer/$orderNumber")
                .willReturn(
                    aResponse()
                        .withProtobufResponse { responseExpected }
                        .withStatus(HttpStatus.ACCEPTED.value())
                )
        )

        // when
        val responseActual = storeClientProto.checkTransfer(orderNumber)

        //  then
        assertThat(responseActual.orderNumber).isEqualTo(orderNumber)
    }
}
