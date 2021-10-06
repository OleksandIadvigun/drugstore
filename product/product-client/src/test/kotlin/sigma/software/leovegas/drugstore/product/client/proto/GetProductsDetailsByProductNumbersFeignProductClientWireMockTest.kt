package sigma.software.leovegas.drugstore.product.client.proto

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import java.math.BigDecimal
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.http.HttpStatus
import org.springframework.test.context.ContextConfiguration
import sigma.software.leovegas.drugstore.api.protobuf.Proto
import sigma.software.leovegas.drugstore.api.toDecimalProto
import sigma.software.leovegas.drugstore.infrastructure.WireMockTest

@SpringBootApplication
internal class GetProductsDetailsByProductNumbersFeignProductClientWireMockTestApp

@DisplayName("Get Products Details by product numbers Feign ProductClient WireMock test")
@ContextConfiguration(classes = [GetProductsDetailsByProductNumbersFeignProductClientWireMockTestApp::class])
class GetProductsDetailsByProductNumbersFeignProductClientWireMockTest @Autowired constructor(
    val productClientProto: ProductClientProto,
    val objectMapper: ObjectMapper
) : WireMockTest() {

    @Test
    fun `should get products by product numbers`() {

        val responseExpected = Proto.ProductDetailsResponse.newBuilder().addAllProducts(
            listOf(
                Proto.ProductDetailsItem.newBuilder()
                    .setProductNumber("1")
                    .setName("test1")
                    .setPrice(BigDecimal.ONE.toDecimalProto())
                    .setQuantity(1)
                    .build()
            )
        ).build()

        // and
        stubFor(
            get("/api/v1/products/details?productNumbers=1")
                .willReturn(
                    aResponse()
                        .withProtobufResponse { responseExpected }
                        .withStatus(HttpStatus.OK.value())
                )
        )

        // when
        val responseActual = productClientProto.getProductsDetailsByProductNumbers(listOf("1"))

        //  then
        assertThat(responseActual.productsList.size).isEqualTo(1)
        assertThat(responseActual.getProducts(0).productNumber).isEqualTo("1")
        assertThat(responseActual.getProducts(0).name).isEqualTo("test1")
        assertThat(responseActual.getProducts(0).price).isEqualTo(BigDecimal.ONE.toDecimalProto())
        assertThat(responseActual.getProducts(0).quantity).isEqualTo(1)

    }
}
