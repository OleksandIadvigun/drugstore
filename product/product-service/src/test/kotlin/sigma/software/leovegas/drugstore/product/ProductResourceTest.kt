package sigma.software.leovegas.drugstore.product

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.google.protobuf.ByteString
import java.math.BigDecimal
import java.time.LocalDateTime
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpMethod.POST
import org.springframework.http.HttpMethod.PUT
import org.springframework.http.HttpStatus
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.api.protobuf.Proto
import sigma.software.leovegas.drugstore.api.toDecimalProto
import sigma.software.leovegas.drugstore.infrastructure.extensions.get
import sigma.software.leovegas.drugstore.infrastructure.extensions.respTypeRef
import sigma.software.leovegas.drugstore.infrastructure.extensions.withProtobufResponse
import sigma.software.leovegas.drugstore.product.api.CreateProductRequest
import sigma.software.leovegas.drugstore.product.api.CreateProductResponse
import sigma.software.leovegas.drugstore.product.api.CreateProductsEvent
import sigma.software.leovegas.drugstore.product.api.GetProductResponse
import sigma.software.leovegas.drugstore.product.api.ProductStatusDTO
import sigma.software.leovegas.drugstore.product.api.SearchProductResponse

@TestInstance(PER_CLASS)
@DisplayName("ProductResource test")
@SpringBootTest(webEnvironment = RANDOM_PORT)
class ProductResourceTest @Autowired constructor(
    val transactionalTemplate: TransactionTemplate,
    val productRepository: ProductRepository,
    val productProperties: ProductProperties,
    val restTemplate: TestRestTemplate,
    @LocalServerPort val port: Int,
) : WireMockTest() {

    lateinit var baseUrl: String

    @BeforeEach
    fun setup() {
        baseUrl = "http://${productProperties.host}:$port"
    }

    @Test
    fun `should get product price`() {

        // setup
        transactionalTemplate.execute {
            productRepository.deleteAll()
        }

        // given
        val productNumber = transactionalTemplate.execute {
            productRepository.save(
                Product(
                    productNumber = "1",
                    name = "A test product",
                    price = BigDecimal("1.23"),
                    quantity = 3,
                )
            )
        }?.productNumber.get()

        // when
        val response = restTemplate.exchange(
            "$baseUrl/api/v1/products/{productNumber}/price",
            GET, null, respTypeRef<Proto.ProductsPrice>(), productNumber
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        // and
        val priceMap = response.body.get()
        assertThat(priceMap.itemsMap.getValue(productNumber)).isEqualTo(BigDecimal("1.23").toDecimalProto())
    }

    @Test
    fun `should create product`() {

        // setup
        transactionalTemplate.execute { productRepository.deleteAll() }

        // given
        val httpEntity = HttpEntity(
            CreateProductsEvent(
                listOf(
                    CreateProductRequest(
                        productNumber = "1",
                        name = "test1",
                        quantity = 1,
                        price = BigDecimal.ONE
                    ),
                    CreateProductRequest(
                        productNumber = "2",
                        name = "test2",
                        quantity = 2,
                        price = BigDecimal.TEN
                    )
                )
            )
        )
        // when
        val response = restTemplate
            .exchange("$baseUrl/api/v1/products", POST, httpEntity, respTypeRef<List<CreateProductResponse>>())

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)

        // and
        val body = response.body.get()
        assertThat(body).hasSize(2)
        assertThat(body[0].createdAt).isBeforeOrEqualTo(LocalDateTime.now())
        assertThat(body[0].status).isEqualTo(ProductStatusDTO.CREATED)
    }

    @Test
    fun `should receive product`() {

        // setup
        transactionalTemplate.execute { productRepository.deleteAll() }

        // given
        val saved = transactionalTemplate.execute {
            productRepository.save(
                Product(
                    productNumber = "1",
                    name = "test",
                    quantity = 10,
                    status = ProductStatus.CREATED
                )
            )
        }.get()

        val httpEntity = HttpEntity(
            Proto.ProductNumberList.newBuilder().addAllProductNumber(listOf(saved.productNumber)).build()
        )

        // when
        val response = restTemplate
            .exchange(
                "$baseUrl/api/v1/products/receive",
                PUT,
                httpEntity,
                respTypeRef<Proto.ReceiveProductResponse>()
            )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.ACCEPTED)

        // and
        val body = response.body.get()
        assertThat(body.productsList).hasSize(1)
        assertThat(body.getProducts(0).productNumber).isEqualTo(saved.productNumber)
        assertThat(body.getProducts(0).status).isEqualTo(Proto.ProductStatusDTO.RECEIVED)
    }

    @Test
    fun `should get products details by product numbers`() {

        // given
        transactionalTemplate.execute {
            productRepository.deleteAll()
        }

        // and
        val productNumbers = transactionalTemplate.execute {
            productRepository.saveAll(
                listOf(
                    Product(
                        productNumber = "1",
                        status = ProductStatus.RECEIVED,
                        name = "test1",
                        price = BigDecimal("20.00"),
                        quantity = 1
                    ),
                    Product(
                        productNumber = "2",
                        status = ProductStatus.RECEIVED,
                        name = "test2",
                        price = BigDecimal("30.00"),
                        quantity = 2
                    )
                )
            ).map { it.productNumber }
        }.get()

        // when
        val response = restTemplate
            .exchange(
                "$baseUrl/api/v1/products/details?" +
                        "productNumbers=${productNumbers[0]}&productNumbers=${productNumbers[1]}",
                GET,
                null,
                respTypeRef<Proto.ProductDetailsResponse>()
            )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        // and
        val body = response.body.get("body")
        assertThat(body.productsList).hasSize(2)
        assertThat(body.getProducts(0).name).isEqualTo("test1")
        assertThat(body.getProducts(0).quantity).isEqualTo(1)
        assertThat(body.getProducts(0).price).isEqualTo(BigDecimal("20.00").toDecimalProto())
        assertThat(body.getProducts(1).name).isEqualTo("test2")
        assertThat(body.getProducts(1).quantity).isEqualTo(2)
        assertThat(body.getProducts(1).price).isEqualTo(BigDecimal("30.00").toDecimalProto())
    }

    @Test
    fun `should get popular products`() {

        // given
        transactionalTemplate.execute {
            productRepository.deleteAll()
        }

        // and
        val saved = transactionalTemplate.execute {
            productRepository.saveAll(
                listOf(
                    Product(
                        productNumber = "1",
                        name = "aspirin",
                        status = ProductStatus.RECEIVED,
                        quantity = 10,
                    ),
                    Product(
                        productNumber = "2",
                        name = "aspirin2",
                        status = ProductStatus.RECEIVED,
                        quantity = 10,
                    ),
                    Product(
                        productNumber = "3",
                        name = "mostPopular",
                        status = ProductStatus.CREATED,
                        quantity = 10,
                    ),
                    Product(
                        productNumber = "4",
                        name = "some2",
                        status = ProductStatus.RECEIVED,
                        quantity = 10,
                    )
                )
            )
        }.get()

        // and
        val responseExpected = Proto.ProductQuantityMap.newBuilder()
            .putProductQuantityItem(saved[2].productNumber, 9)
            .putProductQuantityItem(saved[1].productNumber, 5)
            .putProductQuantityItem(saved[0].productNumber, 1)
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

        // when
        val response = restTemplate
            .exchange(
                "$baseUrl/api/v1/products/popular",
                GET,
                null,
                respTypeRef<List<GetProductResponse>>()
            )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        // and
        val body = response.body.get("body")
        assertThat(body).hasSize(2)
        assertThat(body[0].productNumber).isEqualTo(saved[1].productNumber)
        assertThat(body[0].name).isEqualTo("aspirin2")
        assertThat(body[1].productNumber).isEqualTo(saved[0].productNumber)
        assertThat(body[1].name).isEqualTo("aspirin")
    }

    @Test
    fun `should search products by search word sorted by price ascendant`() {

        // given
        transactionalTemplate.execute {
            productRepository.deleteAllInBatch()
        }

        // and

        val saved = transactionalTemplate.execute {
            productRepository.saveAll(
                listOf(
                    Product(
                        productNumber = "1",
                        name = "aspirin",
                        price = BigDecimal("10.00"),
                        quantity = 10,
                        status = ProductStatus.RECEIVED
                    ),
                    Product(
                        productNumber = "2",
                        name = "aspirin2",
                        price = BigDecimal("50.00"),
                        quantity = 10,
                        status = ProductStatus.RECEIVED
                    ),
                    Product(
                        productNumber = "3",
                        name = "aspirin",
                        price = BigDecimal("5.00"),
                        quantity = 10,
                        status = ProductStatus.CREATED
                    ),
                    Product(
                        productNumber = "4",
                        name = "some2",
                        price = BigDecimal("30.00"),
                        quantity = 10,
                        status = ProductStatus.RECEIVED
                    )
                )
            )
        }.get()

        // and
        val price = BigDecimal("20.00")
        val price2 = BigDecimal("100.00")
        val protoPrice = Proto.DecimalValue.newBuilder()
            .setPrecision(price.precision())
            .setScale(price.scale())
            .setValue(ByteString.copyFrom(price.unscaledValue().toByteArray()))
            .build()
        val protoPrice2 = Proto.DecimalValue.newBuilder()
            .setPrecision(price2.precision())
            .setScale(price2.scale())
            .setValue(ByteString.copyFrom(price2.unscaledValue().toByteArray()))
            .build()
        val responseEProto = Proto.ProductsPrice.newBuilder()
            .putItems(saved[1].productNumber, protoPrice2)
            .putItems(saved[0].productNumber, protoPrice)
            .build()

        // and
        stubFor(
            WireMock.get(
                "/api/v1/accountancy/sale-price?" +
                        "productNumbers=${saved[0].productNumber}&productNumbers=${saved[1].productNumber}"
            )
                .willReturn(
                    aResponse()
                        .withProtobufResponse { responseEProto }
                        .withStatus(HttpStatus.OK.value())
                )
        )

        // when
        val response = restTemplate
            .exchange(
                "$baseUrl/api/v1/products/search?search=aspirin&sortField=price&sortDirection=ASC",
                GET,
                null,
                respTypeRef<List<SearchProductResponse>>()
            )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        // and
        val body = response.body.get("body")
        assertThat(body).hasSize(2)
        assertThat(body[0].productNumber).isEqualTo(saved[0].productNumber)
        assertThat(body[0].price).isEqualTo(BigDecimal("20.00"))
        assertThat(body[1].productNumber).isEqualTo(saved[1].productNumber)
        assertThat(body[1].price).isEqualTo(BigDecimal("100.00"))
    }

    @Test
    fun `should search products by search word sorted by price descendant`() {

        // given
        transactionalTemplate.execute {
            productRepository.deleteAllInBatch()
        }

        // and

        val saved = transactionalTemplate.execute {
            productRepository.saveAll(
                listOf(
                    Product(
                        productNumber = "1",
                        name = "aspirin",
                        price = BigDecimal("10.00"),
                        quantity = 10,
                        status = ProductStatus.RECEIVED
                    ),
                    Product(
                        productNumber = "2",
                        name = "aspirin2",
                        price = BigDecimal("50.00"),
                        quantity = 10,
                        status = ProductStatus.RECEIVED
                    ),
                    Product(
                        productNumber = "3",
                        name = "aspirin",
                        price = BigDecimal("5.00"),
                        quantity = 10,
                        status = ProductStatus.CREATED
                    ),
                    Product(
                        productNumber = "4",
                        name = "some2",
                        price = BigDecimal("30.00"),
                        quantity = 10,
                        status = ProductStatus.RECEIVED
                    )
                )
            )
        }.get()

        // and
        val price = BigDecimal("20.00")
        val price2 = BigDecimal("100.00")
        val protoPrice = Proto.DecimalValue.newBuilder()
            .setPrecision(price.precision())
            .setScale(price.scale())
            .setValue(ByteString.copyFrom(price.unscaledValue().toByteArray()))
            .build()
        val protoPrice2 = Proto.DecimalValue.newBuilder()
            .setPrecision(price2.precision())
            .setScale(price2.scale())
            .setValue(ByteString.copyFrom(price2.unscaledValue().toByteArray()))
            .build()
        val responseEProto = Proto.ProductsPrice.newBuilder()
            .putItems(saved[1].productNumber, protoPrice2)
            .putItems(saved[0].productNumber, protoPrice)
            .build()

        // and
        stubFor(
            WireMock.get(
                "/api/v1/accountancy/sale-price?" +
                        "productNumbers=${saved[1].productNumber}&productNumbers=${saved[0].productNumber}"
            )
                .willReturn(
                    aResponse()
                        .withProtobufResponse { responseEProto }
                        .withStatus(HttpStatus.OK.value())
                )
        )

        // when
        val response = restTemplate
            .exchange(
                "$baseUrl/api/v1/products/search?search=aspirin&sortField=price&sortDirection=DESC",
                GET,
                null,
                respTypeRef<List<SearchProductResponse>>()
            )
        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        // and
        val body = response.body.get("body")
        assertThat(body).hasSize(2)
        assertThat(body[0].productNumber).isEqualTo(saved[1].productNumber)
        assertThat(body[0].price).isEqualTo(BigDecimal("100.00"))
        assertThat(body[1].productNumber).isEqualTo(saved[0].productNumber)
        assertThat(body[1].price).isEqualTo(BigDecimal("20.00"))
    }

    @Test
    fun `should search products by search word sorted by popularity descendant`() {

        // given
        transactionalTemplate.execute {
            productRepository.deleteAllInBatch()
        }

        // and
        val productNumbers = transactionalTemplate.execute {
            productRepository.saveAll(
                listOf(
                    Product(
                        productNumber = "1",
                        name = "aspirin",
                        quantity = 10,
                        status = ProductStatus.RECEIVED
                    ),
                    Product(
                        productNumber = "2",
                        name = "aspirin2",
                        quantity = 10,
                        status = ProductStatus.RECEIVED

                    ),
                    Product(
                        productNumber = "3",
                        name = "aspirin",
                        quantity = 10,
                        status = ProductStatus.CREATED
                    ),
                    Product(
                        productNumber = "4",
                        name = "some2",
                        quantity = 10,
                        status = ProductStatus.RECEIVED

                    )
                )
            )
        }?.map { it.productNumber }.get()

        //and
        val responseExpected = Proto.ProductQuantityMap.newBuilder()
            .putProductQuantityItem(productNumbers[2], 9)
            .putProductQuantityItem(productNumbers[1], 5)
            .putProductQuantityItem(productNumbers[0], 1)
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
        val price = BigDecimal("40.00")
        val price2 = BigDecimal("20.00")
        val protoPrice = Proto.DecimalValue.newBuilder()
            .setPrecision(price.precision())
            .setScale(price.scale())
            .setValue(ByteString.copyFrom(price.unscaledValue().toByteArray()))
            .build()
        val protoPrice2 = Proto.DecimalValue.newBuilder()
            .setPrecision(price2.precision())
            .setScale(price2.scale())
            .setValue(ByteString.copyFrom(price2.unscaledValue().toByteArray()))
            .build()
        val responseEProto = Proto.ProductsPrice.newBuilder()
            .putItems(productNumbers[1], protoPrice2)
            .putItems(productNumbers[0], protoPrice)
            .build()

        // and
        stubFor(
            WireMock.get(
                "/api/v1/accountancy/sale-price?" +
                        "productNumbers=${productNumbers[0]}&productNumbers=${productNumbers[1]}"
            )
                .willReturn(
                    aResponse()
                        .withProtobufResponse { responseEProto }
                        .withStatus(HttpStatus.OK.value())
                )
        )

        // when
        val response = restTemplate
            .exchange(
                "$baseUrl/api/v1/products/search?search=aspirin&sortField=popularity&sortDirection=ASC",
                GET,
                null,
                respTypeRef<List<SearchProductResponse>>()
            )
        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        // and
        val body = response.body.get("body")
        assertThat(body).hasSize(2)
        assertThat(body[0].productNumber).isEqualTo(productNumbers[1])
        assertThat(body[1].productNumber).isEqualTo(productNumbers[0])
        assertThat(body[0].price).isEqualTo(BigDecimal("20.00"))
        assertThat(body[1].price).isEqualTo(BigDecimal("40.00"))
    }

    @Test
    fun `should deliver products`() {

        // setup
        transactionalTemplate.execute {
            productRepository.deleteAll()
        }

        // given
        val saved = transactionalTemplate.execute {
            productRepository.save(
                Product(
                    productNumber = "1",
                    name = "test",
                    quantity = 10
                )
            )
        }.get()

        // and
        val items = listOf(
            Proto.Item.newBuilder().setProductNumber(saved.productNumber).setQuantity(3).build()
        )

        // and
        val httpEntity = HttpEntity(Proto.DeliverProductsDTO.newBuilder().addAllItems(items).build())

        // when
        val response = restTemplate
            .exchange(
                "$baseUrl/api/v1/products/deliver",
                PUT,
                httpEntity,
                respTypeRef<Proto.DeliverProductsDTO>()
            )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.ACCEPTED)

        // and
        val body = response.body.get("body")
        assertThat(body.itemsList).hasSize(1)
        assertThat(body.getItems(0).productNumber).isEqualTo(saved.productNumber)
        assertThat(body.getItems(0).quantity).isEqualTo(7)  // 10 - 3
    }
}
