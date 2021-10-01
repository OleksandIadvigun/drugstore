package sigma.software.leovegas.drugstore.product.client.proto

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.google.protobuf.ByteString
import java.math.BigDecimal
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.http.HttpStatus
import org.springframework.test.context.ContextConfiguration
import sigma.software.leovegas.drugstore.api.protobuf.ProtoProductsPrice
import sigma.software.leovegas.drugstore.api.toDecimalPriceProto
import sigma.software.leovegas.drugstore.api.toDecimalProto
import sigma.software.leovegas.drugstore.product.client.WireMockTest

@SpringBootApplication
internal class GetProductsPriceFeignClientWireMockTestApp

@DisplayName("Receive Product Feign Client WireMock test")
@ContextConfiguration(classes = [ReceiveProductFeignClientWireMockTestApp::class])
class GetProductsPriceFeignClientWireMockTest @Autowired constructor(
    val productClientProto: ProductClientProto,
    val objectMapper: ObjectMapper
) : WireMockTest() {

    @Test
    fun `should get products prices`() {

        // given
        val price = BigDecimal("20.00")
        val protoPrice = ProtoProductsPrice.DecimalValue.newBuilder()
            .setPrecision(price.precision())
            .setScale(price.scale())
            .setValue(ByteString.copyFrom(price.unscaledValue().toByteArray()))
            .build()
        val responseExpected = ProtoProductsPrice.ProductsPrice.newBuilder()
            .putItems("1", protoPrice)
            .build()

        //and
        stubFor(
            get("/api/v1/products/1/price")
                .willReturn(
                    aResponse()
                        .withProtobufResponse { responseExpected }
                        .withStatus(HttpStatus.OK.value())
                )
        )

        // when
        val responseActual = productClientProto.getProductPrice(listOf("1"))

        //  then
        assertThat(responseActual.itemsMap.get("1")).isEqualTo(BigDecimal("20.00").toDecimalPriceProto())
    }
}