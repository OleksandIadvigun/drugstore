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
import sigma.software.leovegas.drugstore.order.api.OrderStatusDTO.CREATED

@SpringBootApplication
internal class GetOrderByIdFeignClientWireMockTestApp

@DisplayName("Get Order By Number Feign Client WireMock test")
@ContextConfiguration(classes = [GetOrderByIdFeignClientWireMockTestApp::class])
class GetOrderByIdFeignClientWireMockTest @Autowired constructor(
    val orderClient: OrderClient,
    val objectMapper: ObjectMapper,
) : WireMockTest() {

    @Test
    fun `should get order by order number`() {


        // given
        val responseExpected = OrderResponse(
            orderNumber = "1",
            orderStatus = CREATED,
            orderItems = listOf(
                OrderItemDTO(
                    productNumber = "1",
                    quantity = 2
                )
            ),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
        )

        // and
        stubFor(
            get("/api/v1/orders/1")
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
        val responseActual = orderClient.getOrderById("1")

        //  then
        assertThat(responseActual.orderNumber).isEqualTo("1")
        assertThat(responseActual.orderStatus).isEqualTo(CREATED)
        assertThat(responseActual.createdAt).isBefore(LocalDateTime.now())
        assertThat(responseActual.updatedAt).isBefore(LocalDateTime.now())
        assertThat(responseActual.orderItems).hasSize(1)

        // and
        assertThat(responseActual.orderItems.iterator().next().productNumber).isEqualTo("1")
        assertThat(responseActual.orderItems.iterator().next().quantity).isEqualTo(2)
    }
}
