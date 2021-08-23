package sigma.software.leovegas.drugstore.order.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.put
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.matching.ContainsPattern
import com.github.tomakehurst.wiremock.matching.EqualToPattern
import java.time.LocalDateTime
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import sigma.software.leovegas.drugstore.order.api.OrderItemDTO
import sigma.software.leovegas.drugstore.order.api.OrderResponse
import sigma.software.leovegas.drugstore.order.api.OrderStatusDTO

@SpringBootApplication
internal class ChaneOrderStatusFeignClientWireMockTestApp

@DisplayName("Change Order Status Feign Client WireMock test")
@ContextConfiguration(classes = [ChaneOrderStatusFeignClientWireMockTestApp::class])
class ChangeOrderStatusFeignClientWireMockTest @Autowired constructor(
    val orderClient: OrderClient,
    val objectMapper: ObjectMapper,
) : WireMockTest() {

    @Test
    fun `should change order status`() {

        // given
        val request = OrderStatusDTO.BOOKED

        // and
        val responseExpected = OrderResponse(
            id = 1L,
            orderStatus = OrderStatusDTO.BOOKED,
            orderItems = listOf(
                OrderItemDTO(
                    productId = 1,
                    quantity = 2
                )
            ),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now().plusMinutes(1),
        )

        // and
        stubFor(
            put("/api/v1/orders/change-status/1")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
                .withRequestBody(
                    EqualToPattern(
                        objectMapper
                            .writerWithDefaultPrettyPrinter()
                            .writeValueAsString(request)
                    )
                )
                .willReturn(
                    aResponse()
                        .withBody(
                            objectMapper
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(responseExpected)
                        )
                        .withStatus(HttpStatus.ACCEPTED.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                )
        )

        // when
        val responseActual = orderClient.changeOrderStatus(1L, request)

        //  then
        assertThat(responseActual.id).isEqualTo(1L)
        assertThat(responseActual.orderStatus).isEqualTo(OrderStatusDTO.BOOKED)
        assertThat(responseActual.orderItems).hasSize(1)

        // and
        assertThat(responseActual.orderItems.iterator().next().productId).isEqualTo(1L)
        assertThat(responseActual.orderItems.iterator().next().quantity).isEqualTo(2)
    }
}
