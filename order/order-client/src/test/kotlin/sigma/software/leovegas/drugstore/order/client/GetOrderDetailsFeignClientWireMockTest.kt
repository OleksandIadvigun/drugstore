package sigma.software.leovegas.drugstore.order.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.matching.ContainsPattern
import java.math.BigDecimal
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import sigma.software.leovegas.drugstore.order.api.OrderDetailsDTO
import sigma.software.leovegas.drugstore.order.api.OrderItemDetailsDTO

@SpringBootApplication
internal class GetOrderDetailsFeignClientWireMockTestApp

@DisplayName("Get OrderDetails Feign Client WireMock test")
@ContextConfiguration(classes = [GetOrderDetailsFeignClientWireMockTestApp::class])
class GetOrderDetailsFeignClientWireMockTest @Autowired constructor(
    val orderClient: OrderClient,
    val objectMapper: ObjectMapper,
) : WireMockTest() {

    @Test
    fun `should get orderDetails`() {

        // given
        val responseExpected = OrderDetailsDTO(
            orderItemDetails = listOf(
                OrderItemDetailsDTO(
                    productId = 1,
                    name = "test1",
                    price = BigDecimal.TEN,
                    quantity = 3,
                )
            ),
            total = BigDecimal("30").setScale(2)
        )

        // and
        stubFor(
            get("/api/v1/orders/1/details")
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
        val responseActual = orderClient.getOrderDetails(1L)

        //  then
        assertThat(responseActual.orderItemDetails).hasSize(1)
        assertThat(responseActual.orderItemDetails.iterator().next().name).isEqualTo("test1")
        assertThat(responseActual.orderItemDetails.iterator().next().productId).isEqualTo(1)
        assertThat(responseActual.orderItemDetails.iterator().next().price).isEqualTo(BigDecimal.TEN)
        assertThat(responseActual.orderItemDetails.iterator().next().quantity).isEqualTo(3)
        assertThat(responseActual.total).isEqualTo(BigDecimal("30").setScale(2))
    }
}
