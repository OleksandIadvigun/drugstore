package sigma.software.leovegas.drugstore.accountancy.client.proto

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
import sigma.software.leovegas.drugstore.api.protobuf.Proto
import sigma.software.leovegas.drugstore.infrastructure.WireMockTest

@SpringBootApplication
internal class GetSalePriceProtoFeignClientWireMockTestApp

@DisplayName("Get products prices Feign Client WireMock test protobuf")
@ContextConfiguration(classes = [GetSalePriceProtoFeignClientWireMockTestApp::class])
class GetSalePriceProtoFeignClientWireMockTest @Autowired constructor(
    val accountancyClientProto: AccountancyClientProto,
    val objectMapper: ObjectMapper
) : WireMockTest() {

    @Test
    fun `should get products prices protobuf`() {

        // given
        val price = BigDecimal("20.00")
        val protoPrice = Proto.DecimalValue.newBuilder()
            .setPrecision(price.precision())
            .setScale(price.scale())
            .setValue(ByteString.copyFrom(price.unscaledValue().toByteArray()))
            .build()
        val responseExpected = Proto.ProductsPrice.newBuilder()
            .putItems("123", protoPrice)
            .build()

        // and
        stubFor(
            get("/api/v1/accountancy/sale-price?productNumbers=123")
                .willReturn(
                    aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withProtobufResponse { responseExpected }
                )
        )

        // when
        val responseActual = accountancyClientProto.getSalePrice(listOf("123"))

        // then

        assertThat(responseActual.itemsMap.get("123")).isEqualTo(protoPrice)
    }
}
