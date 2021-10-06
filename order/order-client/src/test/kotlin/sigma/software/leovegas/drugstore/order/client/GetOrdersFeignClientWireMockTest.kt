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
import sigma.software.leovegas.drugstore.infrastructure.WireMockTest
import sigma.software.leovegas.drugstore.order.api.OrderItemDTO
import sigma.software.leovegas.drugstore.order.api.OrderResponse
import sigma.software.leovegas.drugstore.order.api.OrderStatusDTO.CREATED

@SpringBootApplication
internal class GetOrdersFeignClientWireMockTestApp

@DisplayName("Get Orders Feign Client WireMock test")
@ContextConfiguration(classes = [GetOrdersFeignClientWireMockTestApp::class])
class GetOrdersFeignClientWireMockTest @Autowired constructor(
    val orderClient: OrderClient,
    val objectMapper: ObjectMapper,
) : WireMockTest() {

    @Test
    fun `should get orders`() {

        // given
        val responseExpected = listOf(
            OrderResponse(
                orderNumber = "1",
                orderStatus = CREATED,
                orderItems = listOf(
                    OrderItemDTO("1", 2)
                ),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
            ),
            OrderResponse(
                orderNumber = "2",
                orderStatus = CREATED,
                orderItems = listOf(
                    OrderItemDTO("3", 4)
                ),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
            )
        )

        // and
        stubFor(
            get("/api/v1/orders?page=0&size=5")
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
        val responseActual = orderClient.getOrders()

        //  then
        assertThat(responseActual).hasSize(2)

    }
}
