package sigma.software.leovegas.drugstore.order.client

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
import sigma.software.leovegas.drugstore.order.client.client.OrderClient

@SpringBootApplication
internal class GetOrderItemsByQuantityFeignClientWireMockTestApp

@DisplayName("Get Order By Id Feign Client WireMock test")
@ContextConfiguration(classes = [GetOrderByIdFeignClientWireMockTestApp::class])
class GetOrderItemsByQuantityFeignClientWireMockTest @Autowired constructor(
    val orderClient: OrderClient,
    val objectMapper: ObjectMapper,
) : WireMockTest() {

    @Test
    fun `should get total buys of products sorted by quantity DESC`() {

        // given
        val responseExpected = mapOf(
            1L to 7,
            2L to 5,
            3L to 3
        )

        // and
        stubFor(
            get("/api/v1/orders/total-buys")
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
        val responseActual = orderClient.getProductsIdToQuantity()

        //  then
        assertThat(responseActual.size).isEqualTo(3)
        assertThat(responseActual[1]).isEqualTo(7)
        assertThat(responseActual[2]).isEqualTo(5)
        assertThat(responseActual[3]).isEqualTo(3)
    }
}
