package sigma.software.leovegas.drugstore.product.client.proto

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.put
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.http.HttpStatus
import org.springframework.test.context.ContextConfiguration
import sigma.software.leovegas.drugstore.api.protobuf.Proto
import sigma.software.leovegas.drugstore.product.client.WireMockTest

@SpringBootApplication
internal class ReceiveProductFeignClientWireMockTestApp

@DisplayName("Receive Product Feign Client WireMock test")
@ContextConfiguration(classes = [ReceiveProductFeignClientWireMockTestApp::class])
class ReceiveProductFeignClientWireMockTest @Autowired constructor(
    val productClientProto: ProductClientProto,
    val objectMapper: ObjectMapper
) : WireMockTest() {

    @Test
    fun `should receive product`() {

        // and
        val request = Proto.ProductNumberList.newBuilder().addProductNumber("1").build()

        //and
        val responseExpected = Proto.ReceiveProductResponse.newBuilder().addProducts(
            Proto.ReceiveProductItemDTO.newBuilder().setProductNumber("1").setStatus(Proto.ProductStatusDTO.RECEIVED)
        ).build()

        //and
        stubFor(
            put("/api/v1/products/receive")
                .withProtobufRequest { request }
                .willReturn(
                    aResponse()
                        .withProtobufResponse { responseExpected }
                        .withStatus(HttpStatus.ACCEPTED.value())
                )
        )

        // when
        val responseActual = productClientProto.receiveProducts(request)

        //  then
        assertThat(responseActual.getProducts(0).productNumber).isEqualTo("1")
        assertThat(responseActual.getProducts(0).status).isEqualTo(Proto.ProductStatusDTO.RECEIVED)
    }
}
