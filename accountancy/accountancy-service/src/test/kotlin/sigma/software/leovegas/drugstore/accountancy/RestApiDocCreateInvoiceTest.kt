package sigma.software.leovegas.drugstore.accountancy

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.put
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import com.github.tomakehurst.wiremock.matching.ContainsPattern
import com.github.tomakehurst.wiremock.matching.EqualToPattern
import java.math.BigDecimal
import org.hamcrest.Matchers.emptyString
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.accountancy.api.InvoiceRequest
import sigma.software.leovegas.drugstore.accountancy.client.AccountancyProperties
import sigma.software.leovegas.drugstore.order.api.OrderDetailsDTO
import sigma.software.leovegas.drugstore.order.api.OrderItemDetailsDTO
import sigma.software.leovegas.drugstore.order.api.OrderResponse
import sigma.software.leovegas.drugstore.order.api.OrderStatusDTO
import sigma.software.leovegas.drugstore.store.api.StoreResponse
import sigma.software.leovegas.drugstore.store.api.UpdateStoreRequest

@DisplayName("Create invoice REST API Doc test")
@AutoConfigureWireMock(port = 8082)
class RestApiDocCreateInvoiceTest @Autowired constructor(
    val objectMapper: ObjectMapper,
    @LocalServerPort val port: Int,
    val accountancyProperties: AccountancyProperties,
    val transactionalTemplate: TransactionTemplate,
    val invoiceRepository: InvoiceRepository
) : RestApiDocumentationTest() {

    private val wireMockServerStoreClient = WireMockServer(wireMockConfig().port(8083))

    @Test
    fun `should create invoice`() {

        // given
        wireMockServerStoreClient.start()

        // and
        transactionalTemplate.execute {
            invoiceRepository.deleteAll()
        } ?: fail("result is expected")
        // and

        val priceItemJson = objectMapper
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(
                InvoiceRequest(
                    orderId = 1
                )
            )

        val orderDetail = OrderDetailsDTO(
            orderItemDetails = listOf(
                OrderItemDetailsDTO(
                    priceItemId = 1,
                    name = "test1",
                    price = BigDecimal("20.00"),
                    quantity = 3,
                ),
                OrderItemDetailsDTO(
                    priceItemId = 2,
                    name = "test2",
                    price = BigDecimal("10.00"),
                    quantity = 3,
                )
            ),
            total = BigDecimal("90").setScale(2)
        )

        stubFor(
            get("/api/v1/orders/1/details")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(
                    aResponse()
                        .withBody(
                            objectMapper
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(orderDetail)
                        )
                        .withStatus(HttpStatus.OK.value())
                )
        )

        stubFor(
            put("/api/v1/orders/change-status/1")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
                .withRequestBody(
                    EqualToPattern(
                        objectMapper
                            .writerWithDefaultPrettyPrinter()
                            .writeValueAsString(OrderStatusDTO.BOOKED)
                    )
                )
                .willReturn(
                    aResponse()
                        .withBody(
                            objectMapper
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(OrderResponse(orderStatus = OrderStatusDTO.BOOKED))
                        )
                        .withStatus(HttpStatus.OK.value())
                )
        )

        // and
        val storeResponse = listOf(
            StoreResponse(
                id = 1L,
                priceItemId = 1L,
                quantity = 2
            )
        )

        wireMockServerStoreClient.stubFor(
            put("/api/v1/store/reduce")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
                .withRequestBody(
                    EqualToPattern(
                        objectMapper
                            .writerWithDefaultPrettyPrinter()
                            .writeValueAsString(
                                listOf(
                                    UpdateStoreRequest(1L, 3),
                                    UpdateStoreRequest(2L, 3)
                                )
                            )
                    )
                )
                .willReturn(
                    aResponse()
                        .withBody(
                            objectMapper
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(storeResponse)
                        )
                        .withStatus(HttpStatus.OK.value())
                )
        )

        of("create-invoice").`when`()
            .body(priceItemJson)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .post("http://${accountancyProperties.host}:$port/api/v1/accountancy/invoice")
            .then()
            .assertThat().statusCode(201)
            .assertThat().body("createdAt", not(emptyString()))
            .assertThat().body("status", equalTo("CREATED"))
            .assertThat().body("total", equalTo(90.0F))
            .assertThat().body("productItems[0].priceItemId", equalTo(1))
            .assertThat().body("productItems[0].quantity", equalTo(3))
            .assertThat().body("productItems[0].price", equalTo(20.0F))

        // and
        wireMockServerStoreClient.stop()
    }
}
