package sigma.software.leovegas.drugstore.product.restdoc

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.google.protobuf.ByteString
import java.math.BigDecimal
import org.hamcrest.Matchers
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.api.protobuf.Proto
import sigma.software.leovegas.drugstore.api.protobuf.ProtoProductsPrice
import sigma.software.leovegas.drugstore.infrastructure.extensions.get
import sigma.software.leovegas.drugstore.infrastructure.extensions.withProtobufResponse
import sigma.software.leovegas.drugstore.product.Product
import sigma.software.leovegas.drugstore.product.ProductProperties
import sigma.software.leovegas.drugstore.product.ProductRepository
import sigma.software.leovegas.drugstore.product.ProductStatus

@DisplayName("Get products REST API Doc test")
class RestApiDocGetProductsTest @Autowired constructor(
    @LocalServerPort val port: Int,
    val transactionTemplate: TransactionTemplate,
    val productRepository: ProductRepository,
    val productProperties: ProductProperties
) : RestApiDocumentationTest(productProperties) {


    @Test
    fun `should get products by search sorted by popularity  `() {

        // given
        transactionTemplate.execute {
            productRepository.deleteAll()
        }

        // and
        val savedProducts = transactionTemplate.execute {
            productRepository.saveAll(
                listOf(
                    Product(
                        productNumber = "1",
                        name = "test",
                        price = BigDecimal("20.00"),
                        quantity = 5,
                        status = ProductStatus.RECEIVED,
                    ),
                    Product(
                        productNumber = "2",
                        name = "test2",
                        price = BigDecimal("30.00"),
                        quantity = 3,
                        status = ProductStatus.RECEIVED,
                    ),
                    Product(
                        productNumber = "3",
                        name = "test3",
                        price = BigDecimal("10.00"),
                        quantity = 7,
                        status = ProductStatus.RECEIVED,
                    )
                )
            )
        }.get()


        // and
        val responseExpected = Proto.ProductQuantityMap.newBuilder()
            .putProductQuantityItem(savedProducts[1].productNumber, 5)
            .putProductQuantityItem(savedProducts[0].productNumber, 1)
            .build()

        // and
        stubFor(
            WireMock.get("/api/v1/orders/total-buys")
                .willReturn(
                    aResponse()
                        .withProtobufResponse { responseExpected }
                        .withStatus(HttpStatus.OK.value())
                )
        )

        // and
        val price = BigDecimal("20.00")
        val price2 = BigDecimal("100.00")
        val protoPrice = ProtoProductsPrice.DecimalValue.newBuilder()
            .setPrecision(price.precision())
            .setScale(price.scale())
            .setValue(ByteString.copyFrom(price.unscaledValue().toByteArray()))
            .build()
        val protoPrice2 = ProtoProductsPrice.DecimalValue.newBuilder()
            .setPrecision(price2.precision())
            .setScale(price2.scale())
            .setValue(ByteString.copyFrom(price2.unscaledValue().toByteArray()))
            .build()
        val responseEProto = ProtoProductsPrice.ProductsPrice.newBuilder()
            .putItems(savedProducts[0].productNumber, protoPrice)
            .putItems(savedProducts[1].productNumber, protoPrice2)
            .build()

        // and
        stubFor(
            WireMock.get(
                "/api/v1/accountancy/sale-price?" +
                        "productNumbers=${savedProducts[0].productNumber}&productNumbers=${savedProducts[1].productNumber}"
            )
                .willReturn(
                    aResponse()
                        .withProtobufResponse { responseEProto }
                        .withStatus(HttpStatus.OK.value())
                )
        )

        of("get-products").`when`()
            .get("http://${productProperties.host}:$port/api/v1/products/search?search=test")
            .then()
            .assertThat().statusCode(200)
            .assertThat().body("size", `is`(2))
            .assertThat().body("[0].name", Matchers.equalTo("test2"))
            .assertThat().body("[0].price", Matchers.equalTo(100.0F))
            .assertThat().body("[0].quantity", Matchers.equalTo(3))
            .assertThat().body("[1].name", Matchers.equalTo("test"))
    }
}
