package sigma.software.leovegas.drugstore.order.restdoc

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.matching.ContainsPattern
import com.github.tomakehurst.wiremock.matching.EqualToPattern
import java.math.BigDecimal
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.MediaType
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.accountancy.api.ConfirmOrderResponse
import sigma.software.leovegas.drugstore.accountancy.api.CreateOutcomeInvoiceRequest
import sigma.software.leovegas.drugstore.accountancy.api.ItemDTO
import sigma.software.leovegas.drugstore.infrastructure.extensions.get
import sigma.software.leovegas.drugstore.order.Order
import sigma.software.leovegas.drugstore.order.OrderItem
import sigma.software.leovegas.drugstore.order.OrderProperties
import sigma.software.leovegas.drugstore.order.OrderRepository
import sigma.software.leovegas.drugstore.order.OrderStatus


@DisplayName("Confirm order REST API Doc test")
class RestApiDocConfirmOrderTest @Autowired constructor(
    @LocalServerPort val port: Int,
    val transactionTemplate: TransactionTemplate,
    val orderRepository: OrderRepository,
    val objectMapper: ObjectMapper,
    val orderProperties: OrderProperties
) : RestApiDocumentationTest(orderProperties) {

    @Test
    fun `should confirm order`() {

        // setup
        transactionTemplate.execute {
            orderRepository.deleteAll()
        }

        // given
        val order = transactionTemplate.execute {
            orderRepository.save(
                Order(
                    orderNumber = "1",
                    orderStatus = OrderStatus.CREATED,
                    orderItems = setOf(
                        OrderItem(
                            productNumber = "1",
                            quantity = 1
                        ),
                    )
                )
            )
        }.get()

        // and
        val request = CreateOutcomeInvoiceRequest(
            listOf(
                ItemDTO(
                    productNumber = "1",
                    quantity = 1
                )
            ),
            order.orderNumber
        )

        // and
        val response = ConfirmOrderResponse(
            orderNumber = order.orderNumber,
            amount = BigDecimal("20.00")
        )

        // and
        stubFor(
            post("/api/v1/accountancy/invoice/outcome")
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
                                .writeValueAsString(response)
                        )
                )
        )

        of("confirm-order").`when`()
            .body(order.orderNumber)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .post("http://${orderProperties.host}:$port/api/v1/orders/confirm")
            .then()
            .assertThat().statusCode(201)
            .assertThat().body("orderNumber", equalTo(order.orderNumber))
            .assertThat().body("amount", equalTo(20.0F))
    }
}
