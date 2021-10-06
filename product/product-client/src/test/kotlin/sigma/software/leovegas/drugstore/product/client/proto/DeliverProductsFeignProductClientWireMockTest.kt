package sigma.software.leovegas.drugstore.product.client.proto

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
import sigma.software.leovegas.drugstore.infrastructure.WireMockTest

@SpringBootApplication
internal class DeliverProductsFeignProductClientWireMockTestApp

@DisplayName("Deliver Product Feign ProductClient WireMock test")
@ContextConfiguration(classes = [DeliverProductsFeignProductClientWireMockTestApp::class])
class DeliverProductsFeignProductClientWireMockTest @Autowired constructor(
    val productClientProto: ProductClientProto,
) : WireMockTest() {

    @Test
    fun `should deliver product`() {

        // and
        val request = Proto.DeliverProductsDTO
            .newBuilder().addItems(
                Proto.Item.newBuilder().setProductNumber("1").setQuantity(3).build()
            ).build()

        //and
        val responseExpected = Proto.DeliverProductsDTO
            .newBuilder().addItems(
                Proto.Item.newBuilder().setProductNumber("1").setQuantity(7).build()
            ).build()

        //and
        stubFor(
            put("/api/v1/products/deliver")
                .withProtobufRequest { request }
                .willReturn(
                    aResponse()
                        .withProtobufResponse { responseExpected }
                        .withStatus(HttpStatus.ACCEPTED.value())
                )
        )

        // when
        val responseActual = productClientProto.deliverProducts(request)

        //  then
        assertThat(responseActual.getItems(0).productNumber).isEqualTo("1")
        assertThat(responseActual.getItems(0).quantity).isEqualTo(7)
    }
}
