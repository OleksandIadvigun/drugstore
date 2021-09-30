package sigma.software.leovegas.drugstore.accountancy.client.proto

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.http.HttpStatus
import org.springframework.test.context.ContextConfiguration
import sigma.software.leovegas.drugstore.accountancy.client.WireMockTest
import sigma.software.leovegas.drugstore.api.protobuf.Proto

@SpringBootApplication
internal class GetInvoiceByOrderIdProtoFeignClientWireMockTestApp

@DisplayName("Get Invoice Details By Order Id Feign Client WireMock test protobuf")
@ContextConfiguration(classes = [GetInvoiceByOrderIdProtoFeignClientWireMockTestApp::class])
class GetInvoiceByOrderIdProtoFeignClientWireMockTest @Autowired constructor(
    val accountancyClientProto: AccountancyClientProto,
    val objectMapper: ObjectMapper
) : WireMockTest() {

    @Test
    fun `should get invoice details by order id protobuf`() {

        // given
        val responseExpected =
            Proto.InvoiceDetails.newBuilder()
                .addItems(
                    Proto.Item
                        .newBuilder()
                        .setProductNumber("123")
                        .setQuantity(2).build()
                )
                .build()

        // and
        stubFor(
            get("/api/v1/accountancy/invoice/details/order-number/1")
                .willReturn(
                    aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withProtobufResponse { responseExpected }
                )
        )

        // when
        val responseActual = accountancyClientProto.getInvoiceDetailsByOrderNumber("1")

        // then
        assertThat(responseActual.getItems(0).productNumber).isEqualTo("123")
        assertThat(responseActual.getItems(0).quantity).isEqualTo(2)
    }
}
