package sigma.software.leovegas.drugstore.product.client

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
import sigma.software.leovegas.drugstore.product.api.DeliverProductsQuantityRequest
import sigma.software.leovegas.drugstore.product.api.DeliverProductsResponse

@SpringBootApplication
internal class DeliverProductsFeignClientWireMockTestApp

@DisplayName("Deliver Product Feign Client WireMock test")
@ContextConfiguration(classes = [DeliverProductsFeignClientWireMockTestApp::class])
class DeliverProductsFeignClientWireMockTest @Autowired constructor(
    val productClient: ProductClient,
    val objectMapper: ObjectMapper
) : WireMockTest() {

    @Test
    fun `should deliver product`() {

        // and
        val request = listOf(
            DeliverProductsQuantityRequest(
                id = 1,
                quantity = 3
            )
        )

        //and
        val responseExpected = listOf(
            DeliverProductsResponse(
                id = 1L,
                quantity = 7,
                updatedAt = LocalDateTime.now()
            )
        )

        //and
        stubFor(
            put("/api/v1/products/deliver")
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
        val responseActual = productClient.deliverProducts(request)

        //  then
        assertThat(responseActual[0].id).isEqualTo(1L)
        assertThat(responseActual[0].quantity).isEqualTo(7)
        assertThat(responseActual[0].updatedAt).isBeforeOrEqualTo(LocalDateTime.now())
    }
}
