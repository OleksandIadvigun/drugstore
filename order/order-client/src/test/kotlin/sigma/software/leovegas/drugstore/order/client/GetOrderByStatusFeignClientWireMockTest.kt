package sigma.software.leovegas.drugstore.order.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.matching.ContainsPattern
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
internal class GetOrderByStatusFeignClientWireMockTestApp

@DisplayName("Get Order By Status Feign Client WireMock test")
@ContextConfiguration(classes = [GetOrderByStatusFeignClientWireMockTestApp::class])
class GetOrderByStatusFeignClientWireMockTest @Autowired constructor(
    val orderClient: OrderClient,
    val objectMapper: ObjectMapper,
) : WireMockTest() {

    @Test
    fun `should get order by status`() {

        // given
        val responseExpected = listOf(
            OrderResponse(
                id = 1L,
                orderStatus = OrderStatusDTO.CREATED,
                orderItems = listOf(
                    OrderItemDTO(
                        priceItemId = 1,
                        quantity = 2
                    )
                ),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
            )
        )

        // and
        stubFor(
            get("/api/v1/orders/status/CREATED")
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
        val responseActual = orderClient.getOrdersByStatus(OrderStatusDTO.CREATED)

        //  then
        assertThat(responseActual[0].id).isEqualTo(1L)
        assertThat(responseActual[0].orderStatus).isEqualTo(OrderStatusDTO.CREATED)
        assertThat(responseActual[0].orderItems).hasSize(1)

        // and
        assertThat(responseActual[0].orderItems.iterator().next().priceItemId).isEqualTo(1L)
        assertThat(responseActual[0].orderItems.iterator().next().quantity).isEqualTo(2)
    }

}
