package sigma.software.leovegas.drugstore.product

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.google.protobuf.ByteString
import java.math.BigDecimal
import java.time.LocalDateTime
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.api.protobuf.Proto
import sigma.software.leovegas.drugstore.api.toDecimalProto
import sigma.software.leovegas.drugstore.infrastructure.WireMockTest
import sigma.software.leovegas.drugstore.infrastructure.extensions.get
import sigma.software.leovegas.drugstore.infrastructure.extensions.withProtobufResponse
import sigma.software.leovegas.drugstore.product.api.CreateProductRequest
import sigma.software.leovegas.drugstore.product.api.CreateProductsEvent
import sigma.software.leovegas.drugstore.product.api.ProductStatusDTO

@AutoConfigureTestDatabase
@DisplayName("Product service test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductServiceTest @Autowired constructor(
    val transactionTemplate: TransactionTemplate,
    val productRepository: ProductRepository,
    val service: ProductService,
) : WireMockTest() {

    @Test
    fun `should get product price`() {

        // setup
        transactionTemplate.execute {
            productRepository.deleteAll()
        }

        // given
        val product = transactionTemplate.execute {
            productRepository.save(
                Product(
                    price = BigDecimal.TEN
                )
            )
        }.get()

        // and
        val productNumbers = listOf(product.productNumber)

        // when
        val actual = service.getProductPrice(productNumbers)

        // when
        assertThat(actual.itemsMap).hasSize(1)
        assertThat(actual.itemsMap.getValue(product.productNumber)).isEqualTo(
            BigDecimal.TEN.setScale(2).toDecimalProto()
        )
    }

    @Test
    fun `should create product`() {

        // setup
        transactionTemplate.execute { productRepository.deleteAll() }

        // given
        val productRequest = CreateProductsEvent(
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

        // when
        val actual = service.createProduct(productRequest)

        // when
        assertThat(actual).hasSize(2)
        assertThat(actual[0].name).isEqualTo("test1")
        assertThat(actual[0].name).isEqualTo("test1")
        assertThat(actual[0].status).isEqualTo(ProductStatusDTO.CREATED)
        assertThat(actual[0].createdAt).isBeforeOrEqualTo(LocalDateTime.now())
        assertThat(actual[1].name).isEqualTo("test2")
        assertThat(actual[1].status).isEqualTo(ProductStatusDTO.CREATED)
        assertThat(actual[1].createdAt).isBeforeOrEqualTo(LocalDateTime.now())

    }

    @Test
    fun `should search products by search word sorted by popularity descendant`() {

        // given
        transactionTemplate.execute {
            productRepository.deleteAllInBatch()
        }

        // and
        val productNumbers = transactionTemplate.execute {
            productRepository.saveAll(
                listOf(
                    Product(
                        productNumber = "1",
                        name = "aspirin",
                        quantity = 10,
                        status = ProductStatus.RECEIVED,
                        price = BigDecimal("20.00")
                    ),
                    Product(
                        productNumber = "2",
                        name = "aspirin2",
                        quantity = 10,
                        status = ProductStatus.RECEIVED,
                        price = BigDecimal("20.00")
                    ),
                    Product(
                        productNumber = "3",
                        name = "aspirin",
                        quantity = 10,
                        status = ProductStatus.CREATED,
                        price = BigDecimal("30.00")
                    ),
                    Product(
                        productNumber = "4",
                        name = "some2",
                        quantity = 10,
                        status = ProductStatus.RECEIVED,
                        price = BigDecimal("40.00")
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
        val protoPrice = Proto.DecimalValue.newBuilder()
            .setPrecision(price.precision())
            .setScale(price.scale())
            .setValue(ByteString.copyFrom(price.unscaledValue().toByteArray()))
            .build()
        val responseEProto = Proto.ProductsPrice.newBuilder()
            .putItems(productNumbers[0], protoPrice)
            .putItems(productNumbers[1], protoPrice)
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
        val all = service.searchProducts(0, 5, "aspirin", "popularity", "DESC")

        // then
        assertThat(all).isNotNull
        assertThat(all).hasSize(2)
        assertThat(all[0].productNumber).isEqualTo(productNumbers[1])
        assertThat(all[1].productNumber).isEqualTo(productNumbers[0])
        assertThat(all[0].price).isEqualTo(BigDecimal("40.00"))
        assertThat(all[1].price).isEqualTo(BigDecimal("40.00"))
    }

    @Test
    fun `should search products by search word sorted by price descendant`() {

        // given
        transactionTemplate.execute {
            productRepository.deleteAllInBatch()
        }

        // and
        val saved = transactionTemplate.execute {
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
                        price = BigDecimal("40.00"),
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
        val all = service.searchProducts(0, 5, "aspirin", "price", "DESC")

        // then
        assertThat(all).isNotNull
        assertThat(all).hasSize(2)
        assertThat(all[0].productNumber).isEqualTo(saved[1].productNumber)
        assertThat(all[0].price).isEqualTo(BigDecimal("100.00"))
        assertThat(all[1].productNumber).isEqualTo(saved[0].productNumber)
        assertThat(all[1].price).isEqualTo(BigDecimal("20.00"))
    }

    @Test
    fun `should search products by search word sorted by price ascendant`() {

        // given
        transactionTemplate.execute {
            productRepository.deleteAllInBatch()
        }

        // and

        val saved = transactionTemplate.execute {
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
        val all = service.searchProducts(0, 5, "aspirin", "price", "ASC")

        // then
        assertThat(all).isNotNull
        assertThat(all).hasSize(2)
        assertThat(all[0].productNumber).isEqualTo(saved[0].productNumber)
        assertThat(all[0].price).isEqualTo(BigDecimal("20.00"))
        assertThat(all[1].productNumber).isEqualTo(saved[1].productNumber)
        assertThat(all[1].price).isEqualTo(BigDecimal("100.00"))
    }

    @Test
    fun `should not search products by empty search word`() {

        // when
        val exception = assertThrows<NotCorrectRequestException> {
            service.searchProducts(0, 5, "", "price", "DESC")
        }

        //then
        assertThat(exception.message).isEqualTo("Search field can not be empty!")
    }

    @Test
    fun `should get popular products`() {

        // given
        transactionTemplate.execute {
            productRepository.deleteAllInBatch()
        }

        // and
        val saved = transactionTemplate.execute {
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
                        name = "mostPopular",
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
        }.get()

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
        val all = service.getPopularProducts(0, 5)

        // then
        assertThat(all).isNotNull
        assertThat(all).hasSize(2)
        assertThat(all[0].productNumber).isEqualTo(saved[1].productNumber)
        assertThat(all[0].name).isEqualTo("aspirin2")
        assertThat(all[1].productNumber).isEqualTo(saved[0].productNumber)
        assertThat(all[1].name).isEqualTo("aspirin")
    }

    @Test
    fun `should get products details by ids`() {

        // given
        transactionTemplate.execute {
            productRepository.deleteAllInBatch()
        }

        // and
        val productNumbers = transactionTemplate.execute {
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
            )
        }?.map { it.productNumber }.get()

        // when
        val products = service.getProductsDetailsByProductNumbers(productNumbers)

        // then
        assertThat(products.productsList).hasSize(2)
        assertThat(products.getProducts(0).name).isEqualTo("test1")
        assertThat(products.getProducts(0).price).isEqualTo(BigDecimal("20.00").toDecimalProto())
        assertThat(products.getProducts(0).quantity).isEqualTo(1)
        assertThat(products.getProducts(1).name).isEqualTo("test2")
        assertThat(products.getProducts(1).price).isEqualTo(BigDecimal("30.00").toDecimalProto())
        assertThat(products.getProducts(1).quantity).isEqualTo(2)
    }

    @Test
    fun `should deliver products`() {

        // setup
        transactionTemplate.execute { productRepository.deleteAll() }

        // given
        val saved = transactionTemplate.execute {
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

        // when
        val actual = service.deliverProducts(Proto.DeliverProductsDTO.newBuilder().addAllItems(items).build())

        // then
        assertThat(actual).isNotNull
        assertThat(actual.getItems(0).productNumber).isEqualTo(saved.productNumber)
        assertThat(actual.getItems(0).quantity).isEqualTo(7)  // 10 - 3
    }

    @Test
    fun `should not reduce products quantity by if not enough`() {

        // setup
        transactionTemplate.execute { productRepository.deleteAll() }

        // given
        val saved = transactionTemplate.execute {
            productRepository.save(
                Product(
                    productNumber = "1",
                    name = "test",
                    quantity = 5
                )
            )
        }.get()

        // and
        val items = listOf(
            Proto.Item.newBuilder().setProductNumber(saved.productNumber).setQuantity(7).build()
        )

        // when
        val exception = assertThrows<NotEnoughQuantityProductException> {
            service.deliverProducts(
                Proto.DeliverProductsDTO.newBuilder().addAllItems(items).build()
            )
        }

        //then
        assertThat(exception.message)
            .isEqualTo("Not enough available quantity of product with product number: ${saved.productNumber}")
    }

    @Test
    fun `should receive products`() {
        // setup
        transactionTemplate.execute {
            productRepository.deleteAllInBatch()
        }

        // given
        val saved = transactionTemplate.execute {
            productRepository.save(
                Product(
                    productNumber = "1",
                    name = "test",
                    quantity = 10,
                    status = ProductStatus.CREATED
                )
            )
        }.get()

        // and
        val productNumbers =
            Proto.ProductNumberList.newBuilder().addAllProductNumber(listOf(saved.productNumber)).build()

        // when
        val actual = service.receiveProducts(productNumbers)

        // then
        assertThat(actual).isNotNull
        assertThat(actual.getProducts(0).productNumber).isEqualTo(saved.productNumber)
        assertThat(actual.getProducts(0).status).isEqualTo(Proto.ProductStatusDTO.RECEIVED)
    }
}
