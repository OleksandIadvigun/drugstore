package sigma.software.leovegas.drugstore.order.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.post
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
import sigma.software.leovegas.drugstore.order.api.CreateOrderRequest
import sigma.software.leovegas.drugstore.order.api.OrderItemDTO
import sigma.software.leovegas.drugstore.order.api.OrderResponse
import sigma.software.leovegas.drugstore.order.api.OrderStatusDTO.CREATED

@SpringBootApplication
internal class CreateOrderFeignClientWireMockTestApp

@DisplayName("Create Order Feign Client WireMock test")
@ContextConfiguration(classes = [CreateOrderFeignClientWireMockTestApp::class])
class CreateOrderFeignClientWireMockTest @Autowired constructor(
    val orderClient: OrderClient,
    val objectMapper: ObjectMapper,
) : WireMockTest() {

    @Test
    fun `should create order`() {

        // given
        val request: CreateOrderRequest = CreateOrderRequest(
            listOf(
                OrderItemDTO(
                    productId = 1L,
                    quantity = 3
                )
            )
        )

        // and
        val responseExpected = OrderResponse(
            id = 1L,
            orderStatus = CREATED,
            orderItems = request.orderItems,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
        )

        // and
        stubFor(
            post("/api/v1/orders")
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
                        .withStatus(HttpStatus.CREATED.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                )
        )

        // when
        val responseActual = orderClient.createOrder(request)

        //  then
        assertThat(responseActual.id).isEqualTo(1L)
        assertThat(responseActual.orderStatus).isEqualTo(CREATED)
        assertThat(responseActual.createdAt).isBefore(LocalDateTime.now())
        assertThat(responseActual.updatedAt).isBefore(LocalDateTime.now())
        assertThat(responseActual.orderItems).hasSize(1)

        // and
        assertThat(responseActual.orderItems.iterator().next().productId).isEqualTo(1L)
        assertThat(responseActual.orderItems.iterator().next().quantity).isEqualTo(3)
    }
}
