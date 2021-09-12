package sigma.software.leovegas.drugstore.product

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.matching.ContainsPattern
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
import org.springframework.http.MediaType
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.infrastructure.extensions.get
import sigma.software.leovegas.drugstore.product.api.CreateProductRequest
import sigma.software.leovegas.drugstore.product.api.DeliverProductsQuantityRequest
import sigma.software.leovegas.drugstore.product.api.ProductStatusDTO
import sigma.software.leovegas.drugstore.product.api.ReturnProductQuantityRequest

@AutoConfigureTestDatabase
@DisplayName("Product service test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductServiceTest @Autowired constructor(
    val service: ProductService,
    val transactionTemplate: TransactionTemplate,
    val repository: ProductRepository,
    val objectMapper: ObjectMapper
) : WireMockTest() {

    @Test
    fun `should get product price`() {

        // setup
        transactionTemplate.execute {
            repository.deleteAll()
        }

        // given
        val product = transactionTemplate.execute {
            repository.save(
                Product(
                    price = BigDecimal.TEN
                )
            )
        }.get()

        // and
        val ids = listOf(product.id)

        // when
        val actual = service.getProductPrice(ids as List<Long>)

        // when
        assertThat(actual).hasSize(1)
        assertThat(actual.getValue(product.id ?: -1)).isEqualTo(BigDecimal.TEN.setScale(2))
    }


    @Test
    fun `should create product`() {

        // given
        val productRequest = listOf(
            CreateProductRequest(
                name = "test1",
                quantity = 1,
                price = BigDecimal.ONE
            ),
            CreateProductRequest(
                name = "test2",
                quantity = 2,
                price = BigDecimal.TEN
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
            repository.deleteAllInBatch()
        }

        // and
        val ids = transactionTemplate.execute {
            repository.saveAll(
                listOf(
                    Product(
                        name = "aspirin",
                        quantity = 10,
                        status = ProductStatus.RECEIVED,
                        price = BigDecimal("10.00")
                    ),
                    Product(
                        name = "aspirin2",
                        quantity = 10,
                        status = ProductStatus.RECEIVED,
                        price = BigDecimal("20.00")
                    ),
                    Product(
                        name = "aspirin",
                        quantity = 10,
                        status = ProductStatus.CREATED,
                        price = BigDecimal("30.00")
                    ),
                    Product(
                        name = "some2",
                        quantity = 10,
                        status = ProductStatus.RECEIVED,
                        price = BigDecimal("40.00")
                    )
                )
            )
        }?.map { it.id }.get()

        //and
        val responseExpected = mapOf(ids[2] to 9, ids[1] to 5, ids[0] to 1)

        // and
        stubFor(
            WireMock.get("/api/v1/orders/total-buys")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(
                    aResponse()
                        .withBody(
                            objectMapper
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(responseExpected)
                        )
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                )
        )

        // and
        stubFor(
            WireMock.get("/api/v1/accountancy/sale-price?ids=${ids[0]}&ids=${ids[1]}")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(
                    aResponse()
                        .withBody(
                            objectMapper
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(
                                    mapOf(
                                        Pair(ids[1], BigDecimal("40.00")), Pair(ids[0], BigDecimal("20.00"))
                                    )
                                )
                        )
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                )
        )

        // when
        val all = service.searchProducts(0, 5, "aspirin", "popularity", "DESC")

        // then
        assertThat(all).isNotNull
        assertThat(all).hasSize(2)
        assertThat(all[0].id).isEqualTo(ids[1])
        assertThat(all[1].id).isEqualTo(ids[0])
        assertThat(all[0].price).isEqualTo(BigDecimal("40.00"))
        assertThat(all[1].price).isEqualTo(BigDecimal("20.00"))
    }

    @Test
    fun `should search products by search word sorted by price descendant`() {

        // given
        transactionTemplate.execute {
            repository.deleteAllInBatch()
        }

        // and

        val saved = transactionTemplate.execute {
            repository.saveAll(
                listOf(
                    Product(
                        name = "aspirin",
                        price = BigDecimal("10.00"),
                        quantity = 10,
                        status = ProductStatus.RECEIVED
                    ),
                    Product(
                        name = "aspirin2",
                        price = BigDecimal("50.00"),
                        quantity = 10,
                        status = ProductStatus.RECEIVED
                    ),
                    Product(
                        name = "aspirin",
                        price = BigDecimal("40.00"),
                        quantity = 10,
                        status = ProductStatus.CREATED
                    ),
                    Product(
                        name = "some2",
                        price = BigDecimal("30.00"),
                        quantity = 10,
                        status = ProductStatus.RECEIVED
                    )
                )
            )
        }.get()

        // and
        stubFor(
            WireMock.get("/api/v1/accountancy/sale-price?ids=${saved[1].id}&ids=${saved[0].id}")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(
                    aResponse()
                        .withBody(
                            objectMapper
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(
                                    mapOf(
                                        Pair(saved[0].id, BigDecimal("100.00")), Pair(saved[1].id, BigDecimal("20.00"))
                                    )
                                )
                        )
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                )
        )

        // when
        val all = service.searchProducts(0, 5, "aspirin", "price", "DESC")

        // then
        assertThat(all).isNotNull
        assertThat(all).hasSize(2)
        assertThat(all[0].id).isEqualTo(saved[1].id)
        assertThat(all[0].price).isEqualTo(BigDecimal("20.00"))
        assertThat(all[1].id).isEqualTo(saved[0].id)
        assertThat(all[1].price).isEqualTo(BigDecimal("100.00"))
    }

    @Test
    fun `should search products by search word sorted by price ascendant`() {

        // given
        transactionTemplate.execute {
            repository.deleteAllInBatch()
        }

        // and

        val saved = transactionTemplate.execute {
            repository.saveAll(
                listOf(
                    Product(
                        name = "aspirin",
                        price = BigDecimal("10.00"),
                        quantity = 10,
                        status = ProductStatus.RECEIVED
                    ),
                    Product(
                        name = "aspirin2",
                        price = BigDecimal("50.00"),
                        quantity = 10,
                        status = ProductStatus.RECEIVED
                    ),
                    Product(
                        name = "aspirin",
                        price = BigDecimal("5.00"),
                        quantity = 10,
                        status = ProductStatus.CREATED
                    ),
                    Product(
                        name = "some2",
                        price = BigDecimal("30.00"),
                        quantity = 10,
                        status = ProductStatus.RECEIVED
                    )
                )
            )
        }.get()

        // and
        stubFor(
            WireMock.get("/api/v1/accountancy/sale-price?ids=${saved[0].id}&ids=${saved[1].id}")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(
                    aResponse()
                        .withBody(
                            objectMapper
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(
                                    mapOf(
                                        Pair(saved[1].id, BigDecimal("100.00")), Pair(saved[0].id, BigDecimal("20.00"))
                                    )
                                )
                        )
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                )
        )

        // when
        val all = service.searchProducts(0, 5, "aspirin", "price", "ASC")

        // then
        assertThat(all).isNotNull
        assertThat(all).hasSize(2)
        assertThat(all[0].id).isEqualTo(saved[0].id)
        assertThat(all[0].price).isEqualTo(BigDecimal("20.00"))
        assertThat(all[1].id).isEqualTo(saved[1].id)
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

//    @Test
//    fun `should not get products if order server is unavailable`() {
//
//        // given
//        transactionTemplate.execute{
//            repository.deleteAllInBatch()
//        }
//
//        // when
//        val exception = assertThrows<OrderServerException> {
//            service.searchProducts(0, 5, "test", "popularity", "DESC")
//        }
//
//        //then
//        assertThat(exception.message).startsWith("Ups... some problems in order service.")
//    }
//
//    @Test
//    fun `should not get popular products if order server is unavailable`() {
//
//        // given
//        transactionTemplate.execute{
//            repository.deleteAllInBatch()
//        }
//
//        // when
//        val exception = assertThrows<OrderServerException> {
//            service.getPopularProducts(0, 5)
//        }
//
//        //then
//        assertThat(exception.message).startsWith("Ups... some problems in order service.")
//    }

    @Test
    fun `should get popular products`() {

        // given
        transactionTemplate.execute {
            repository.deleteAllInBatch()
        }

        // and
        val saved = transactionTemplate.execute {
            repository.saveAll(
                listOf(
                    Product(
                        name = "aspirin",
                        quantity = 10,
                        status = ProductStatus.RECEIVED
                    ),
                    Product(
                        name = "aspirin2",
                        quantity = 10,
                        status = ProductStatus.RECEIVED

                    ),
                    Product(
                        name = "mostPopular",
                        quantity = 10,
                        status = ProductStatus.CREATED
                    ),
                    Product(
                        name = "some2",
                        quantity = 10,
                        status = ProductStatus.RECEIVED
                    )
                )
            )
        }.get()

        //and
        val responseExpected = mapOf(saved[2].id to 9, saved[1].id to 5, saved[0].id to 1)

        // and
        stubFor(
            WireMock.get("/api/v1/orders/total-buys")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(
                    aResponse()
                        .withBody(
                            objectMapper
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(responseExpected)
                        )
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                )
        )

        // when
        val all = service.getPopularProducts(0, 5)

        // then
        assertThat(all).isNotNull
        assertThat(all).hasSize(2)
        assertThat(all[0].id).isEqualTo(saved[1].id)
        assertThat(all[0].name).isEqualTo("aspirin2")
        assertThat(all[1].id).isEqualTo(saved[0].id)
        assertThat(all[1].name).isEqualTo("aspirin")
    }

    @Test
    fun `should get products details by ids`() {

        // given
        transactionTemplate.execute {
            repository.deleteAllInBatch()
        }

        // and
        val ids = transactionTemplate.execute {
            repository.saveAll(
                listOf(
                    Product(
                        status = ProductStatus.RECEIVED,
                        name = "test1",
                        price = BigDecimal("20.00"),
                        quantity = 1
                    ),
                    Product(
                        status = ProductStatus.RECEIVED,
                        name = "test2",
                        price = BigDecimal("30.00"),
                        quantity = 2
                    )
                )
            ).map { it.id }
        }

        // when
        val products = service.getProductsDetailsByIds(ids as List<Long>)

        // then
        assertThat(products).hasSize(2)
        assertThat(products[0].name).isEqualTo("test1")
        assertThat(products[0].price).isEqualTo(BigDecimal("20.00"))
        assertThat(products[0].quantity).isEqualTo(1)
        assertThat(products[1].name).isEqualTo("test2")
        assertThat(products[1].price).isEqualTo(BigDecimal("30.00"))
        assertThat(products[1].quantity).isEqualTo(2)
    }


    @Test
    fun `should deliver products`() {

        // given
        val saved = transactionTemplate.execute {
            repository.save(
                Product(
                    name = "test",
                    quantity = 10
                )
            )
        }.get()

        // and
        val updatedProductRequest = DeliverProductsQuantityRequest(
            id = saved.id ?: -1,
            quantity = 3
        )

        // when
        val actual = service.deliverProducts(listOf(updatedProductRequest))

        // then
        assertThat(actual).isNotNull
        assertThat(actual[0].quantity).isEqualTo(7)  // 10 - 3
        assertThat(actual[0].updatedAt).isBeforeOrEqualTo(LocalDateTime.now())
    }

    @Test
    fun `should not reduce products quantity by if not enough`() {

        // given
        val saved = transactionTemplate.execute {
            repository.save(
                Product(
                    name = "test",
                    quantity = 5
                )
            )
        }.get()

        // when
        val exception = assertThrows<NotEnoughQuantityProductException> {
            service.deliverProducts(
                listOf(
                    DeliverProductsQuantityRequest(
                        id = saved.id ?: -1,
                        quantity = 7
                    )
                )
            )
        }

        //then
        assertThat(exception.message).isEqualTo("Not enough available quantity of product with id: ${saved.id}")
    }

    @Test
    fun `should receive products`() {

        // given
        val saved = transactionTemplate.execute {
            repository.save(
                Product(
                    name = "test",
                    quantity = 10,
                    status = ProductStatus.CREATED
                )
            )
        }.get()

        // when
        val actual = service.receiveProducts(listOf(saved.id ?: -1))

        // then
        assertThat(actual).isNotNull
        assertThat(actual[0].status).isEqualTo(ProductStatusDTO.RECEIVED)
    }

    @Test
    fun `should return products`() {

        // given
        val saved = transactionTemplate.execute {
            repository.save(
                Product(
                    name = "test",
                    quantity = 10
                )
            )
        }.get()

        // and
        val updatedProductRequest = ReturnProductQuantityRequest(
            id = saved.id ?: -1,
            quantity = 3
        )

        // when
        val actual = service.returnProducts(listOf(updatedProductRequest))

        // then
        assertThat(actual).isNotNull
        assertThat(actual[0].quantity).isEqualTo(13)  // 10 + 3
        assertThat(actual[0].updatedAt).isBeforeOrEqualTo(LocalDateTime.now())
    }
}
