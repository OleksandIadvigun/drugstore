package sigma.software.leovegas.drugstore.store.restdoc

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.matching.ContainsPattern
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import sigma.software.leovegas.drugstore.product.api.DeliverProductsQuantityRequest
import sigma.software.leovegas.drugstore.product.api.ProductDetailsResponse
import sigma.software.leovegas.drugstore.store.StoreProperties

@DisplayName("Check availability REST API Doc test")
class RestApiDocCheckAvailabilityTest @Autowired constructor(
    val objectMapper: ObjectMapper,
    @LocalServerPort val port: Int,
    val storeProperties: StoreProperties,
) : RestApiDocumentationTest(storeProperties) {

    @Test
    fun `should check availability quantity`() {

        // given
        val products = listOf(
            DeliverProductsQuantityRequest(
                id = 1,
                quantity = 2
            ),
            DeliverProductsQuantityRequest(
                id = 2,
                quantity = 3
            )
        )

        val body = objectMapper
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(
                products
            )

        // and
        val productResponse = listOf(
            ProductDetailsResponse(
                id = 1,
                quantity = 10
            ),
            ProductDetailsResponse(
                id = 2,
                quantity = 15
            )
        )

        //and
        stubFor(
            get("/api/v1/products/details?ids=${productResponse[0].id}&ids=${productResponse[1].id}")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(
                    aResponse()
                        .withBody(
                            objectMapper
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(productResponse)
                        )
                        .withStatus(HttpStatus.ACCEPTED.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                )
        )

        of("check-availability")
            .`when`()
            .body(body)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .put("http://${storeProperties.host}:$port/api/v1/store/availability")
            .then()
            .assertThat().statusCode(202)
            .assertThat().body("size()", `is`(2))
            .assertThat().body("[0].id", `is`(1))
            .assertThat().body("[1].id", `is`(2))
    }
}
