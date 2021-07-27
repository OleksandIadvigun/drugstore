package sigma.software.leovegas.drugstore.order

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.matching.ContainsPattern
import com.github.tomakehurst.wiremock.matching.EqualToPattern
import java.math.BigDecimal
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod.POST
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import sigma.software.leovegas.drugstore.infrastructure.WireMockTest
import sigma.software.leovegas.drugstore.infrastructure.extensions.respTypeRef

@Disabled
class ThinkBeforeDoingTest @Autowired constructor(val restTemplate: TestRestTemplate, objectMapper: ObjectMapper) :
    WireMockTest() {

    val requestJson = objectMapper.json(
        OrderDetailsRequest {
            productId = 1L
            quantity = 3
        },
    )

    val responseJson = objectMapper.json {
        id = 1L
        orderDetailsList = listOf(
            OrderDetailsResponse(
                productId = 1L, name = "paracetomol",
                price = BigDecimal.valueOf(10.50), quantity = 3
            )
        )
        total = BigDecimal.valueOf(31.50)
    }

    @Test
    fun `should create order`() {
        // setup
        stubFor(
            post("/orders")
                .withRequestBody(EqualToPattern(requestJson))
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(
                    aResponse()
                        .withBody(responseJson)
                        .withStatus(HttpStatus.CREATED.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                )
        )

        // given
        val httpEntity = HttpEntity(
            OrderRequest(
                listOf(
                    OrderDetailsRequest(1L, 3)
                )
            )
        )

        // when
        val response = restTemplate.exchange("/orders", POST, httpEntity, respTypeRef<OrderResponse>())

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
    }
}
