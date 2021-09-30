package sigma.software.leovegas.drugstore.order.restdoc

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.matching.ContainsPattern
import java.math.BigDecimal
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.api.protobuf.Proto
import sigma.software.leovegas.drugstore.api.toDecimalProto
import sigma.software.leovegas.drugstore.infrastructure.extensions.get
import sigma.software.leovegas.drugstore.infrastructure.extensions.withProtobufResponse
import sigma.software.leovegas.drugstore.order.Order
import sigma.software.leovegas.drugstore.order.OrderItem
import sigma.software.leovegas.drugstore.order.OrderProperties
import sigma.software.leovegas.drugstore.order.OrderRepository
import sigma.software.leovegas.drugstore.order.OrderStatus
import sigma.software.leovegas.drugstore.product.api.ProductDetailsResponse

@DisplayName("Get orderDetails REST API Doc test")
class RestApiDocGetOrderDetailsTest @Autowired constructor(
    @LocalServerPort val port: Int,
    val transactionTemplate: TransactionTemplate,
    val orderRepository: OrderRepository,
    val objectMapper: ObjectMapper,
    val orderProperties: OrderProperties
) : RestApiDocumentationTest(orderProperties) {

    @Test
    fun `should get order details`() {

        // given
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
                        OrderItem(
                            productNumber = "2",
                            quantity = 2
                        )
                    )
                )
            )
        }.get()

        // and
        val response = listOf(
            ProductDetailsResponse(
                productNumber = "1",
                name = "test1",
                quantity = 3,
                price = BigDecimal.ONE
            ),
            ProductDetailsResponse(
                productNumber = "2",
                name = "test2",
                quantity = 4,
                price = BigDecimal.TEN
            )
        )

        // and
        val productsProto = listOf(
            Proto.ProductDetailsItem.newBuilder()
                .setName("test1").setProductNumber("1").setQuantity(3)
                .setPrice(BigDecimal("20.00").toDecimalProto())
                .build(),
            Proto.ProductDetailsItem.newBuilder()
                .setName("test2").setProductNumber("2").setQuantity(4)
                .setPrice(BigDecimal("30.00").toDecimalProto())
                .build()
        )
        Proto.ProductDetailsResponse.newBuilder().addAllProducts(productsProto).build()

        // given
        stubFor(
            WireMock.get("/api/v1/products/details?productNumbers=1&productNumbers=2")
                .willReturn(
                    aResponse()
                        .withProtobufResponse {
                            Proto.ProductDetailsResponse.newBuilder().addAllProducts(productsProto).build()
                        }
                )
        )

        // and
        stubFor(
            WireMock.get("/api/v1/accountancy/sale-price?productNumbers=1&productNumbers=2")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(
                    aResponse()
                        .withBody(
                            objectMapper
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(
                                    mapOf(
                                        Pair("1", BigDecimal("40.00")), Pair("2", BigDecimal("60.00"))
                                    )
                                )
                        )
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                )
        )

        // and
        stubFor(
            WireMock.get("/api/v1/accountancy/sale-price?productNumbers=2&productNumbers=1")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(
                    aResponse()
                        .withBody(
                            objectMapper
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(
                                    mapOf(
                                        Pair("1", BigDecimal("40.00")), Pair("2", BigDecimal("60.00"))
                                    )
                                )
                        )
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                )
        )

        of("get-order-details")
            .pathParam("orderNumber", order.orderNumber).`when`()
            .get("http://${orderProperties.host}:$port/api/v1/orders/{orderNumber}/details")
            .then()
            .assertThat().statusCode(200)
            .assertThat().body("orderNumber", equalTo("1"))
            .assertThat().body("orderItemDetails[0].name", equalTo("test1"))
            .assertThat().body("orderItemDetails[0].quantity", equalTo(1))
            .assertThat().body("orderItemDetails[0].price", equalTo(40.00F))
            .assertThat().body("total", equalTo(160.00F)) // price multiply quantity
    }
}
