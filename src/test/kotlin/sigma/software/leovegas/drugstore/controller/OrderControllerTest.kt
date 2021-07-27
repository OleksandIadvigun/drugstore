package sigma.software.leovegas.drugstore.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.github.tomakehurst.wiremock.matching.EqualToPattern
import java.math.BigDecimal

import org.junit.jupiter.api.Test

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import sigma.software.leovegas.drugstore.dto.OrderDetailsRequest
import sigma.software.leovegas.drugstore.dto.OrderDetailsResponse
import sigma.software.leovegas.drugstore.dto.OrderRequest
import sigma.software.leovegas.drugstore.dto.OrderResponse
import kotlin.test.assertEquals


@SpringBootTest()
@AutoConfigureTestDatabase
class OrderControllerTest @Autowired constructor(
    private val orderController: OrderController,
    private val objectMapper: ObjectMapper,
) {
     val wireMockServer = WireMockServer(8080)



    @Test
    fun `should create order`() {
        wireMockServer.start()
        // given
        val request = OrderRequest(
            orderDetailsList = listOf(
                OrderDetailsRequest(productId = 1L, quantity = 3)
            )
        )

        // and
        stubFor(
            post("/orders")
                .withRequestBody(
                    EqualToPattern(
                        objectMapper.json(request)
                    )
                )
                .willReturn(
                    aResponse()
                        .withStatus(HttpStatus.CREATED.value())
                        .withBody(
                            objectMapper.json(
                                OrderResponse(
                                    id = 1L,
                                    orderDetailsList = listOf(
                                        OrderDetailsResponse(
                                            productId = 1L, name = "paracetomol",
                                            price = BigDecimal.valueOf(10.50), quantity = 3
                                        )
                                    ),
                                    total = BigDecimal.valueOf(31.50)
                                )
                            )
                        )
                )
        )

        // when


        wireMockServer.stop()

        // then

    }
}

private fun ObjectMapper.json(response: OrderResponse): String? {
    return this.writeValueAsString(response)
}

private fun ObjectMapper.json(request: OrderRequest): String? {
    return this.writeValueAsString(request)
}
