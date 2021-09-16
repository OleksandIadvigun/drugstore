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
import sigma.software.leovegas.drugstore.order.api.OrderStatusDTO.UPDATED
import sigma.software.leovegas.drugstore.order.api.UpdateOrderRequest

@SpringBootApplication
internal class UpdateOrderFeignClientWireMockTestApp

@DisplayName("Update Order Feign Client WireMock test")
@ContextConfiguration(classes = [UpdateOrderFeignClientWireMockTestApp::class])
class UpdateOrderFeignClientWireMockTest @Autowired constructor(
    val orderClient: OrderClient,
    val objectMapper: ObjectMapper,
) : WireMockTest() {

    @Test
    fun `should update order`() {

        // given
        val request = UpdateOrderRequest(
            listOf(
                OrderItemDTO(
                    productNumber = "1",
                    quantity = 4
                )
            )
        )

        // and
        val responseExpected = OrderResponse(
            orderNumber = "1",
            orderStatus = UPDATED,
            orderItems = request.orderItems,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now().plusMinutes(1),
        )

        // and
        stubFor(
            put("/api/v1/orders/1")
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
        val responseActual = orderClient.updateOrder("1", request)

        //  then
        assertThat(responseActual.orderNumber).isEqualTo("1")
        assertThat(responseActual.orderStatus).isEqualTo(UPDATED)
        assertThat(responseActual.createdAt).isBefore(LocalDateTime.now())
        assertThat(responseActual.updatedAt).isAfter(LocalDateTime.now())
        assertThat(responseActual.orderItems).hasSize(1)

        // and
        assertThat(responseActual.orderItems.iterator().next().productNumber).isEqualTo("1")
        assertThat(responseActual.orderItems.iterator().next().quantity).isEqualTo(4)
    }
}
