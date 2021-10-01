package sigma.software.leovegas.drugstore.order.client.proto

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
import sigma.software.leovegas.drugstore.api.protobuf.Proto
import sigma.software.leovegas.drugstore.order.client.GetOrderByIdFeignClientWireMockTestApp
import sigma.software.leovegas.drugstore.order.client.WireMockTest

@SpringBootApplication
internal class GetOrderItemsByQuantityFeignClientWireMockTestApp

@DisplayName("Get total buys Feign Client WireMock test")
@ContextConfiguration(classes = [GetOrderByIdFeignClientWireMockTestApp::class])
class GetOrderItemsByQuantityFeignClientWireMockTest @Autowired constructor(
    val orderClientProto: OrderClientProto,
    val objectMapper: ObjectMapper,
) : WireMockTest() {

    @Test
    fun `should get total buys of products sorted by quantity DESC`() {

        // given
        val responseExpected = Proto.ProductQuantityMap.newBuilder()
            .putProductQuantityItem("1", 7)
            .putProductQuantityItem("2", 5)
            .putProductQuantityItem("3", 3)
            .build()

        // and
        stubFor(
            get("/api/v1/orders/total-buys")
                .willReturn(
                    aResponse()
                        .withProtobufResponse { responseExpected }
                        .withStatus(HttpStatus.OK.value())
                )
        )

        // when
        val responseActual = orderClientProto.getProductsIdToQuantity()

        //  then
        assertThat(responseActual.productQuantityItemMap.size).isEqualTo(3)
        assertThat(responseActual.productQuantityItemMap.get("1")).isEqualTo(7)
        assertThat(responseActual.productQuantityItemMap.get("2")).isEqualTo(5)
        assertThat(responseActual.productQuantityItemMap.get("3")).isEqualTo(3)
    }
}
