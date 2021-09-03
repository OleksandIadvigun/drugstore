package sigma.software.leovegas.drugstore.product

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.matching.ContainsPattern
import java.math.BigDecimal
import java.time.LocalDateTime
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
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
import org.springframework.http.MediaType
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.infrastructure.extensions.get
import sigma.software.leovegas.drugstore.infrastructure.extensions.respTypeRef
import sigma.software.leovegas.drugstore.product.api.CreateProductRequest
import sigma.software.leovegas.drugstore.product.api.CreateProductResponse
import sigma.software.leovegas.drugstore.product.api.DeliverProductsQuantityRequest
import sigma.software.leovegas.drugstore.product.api.DeliverProductsResponse
import sigma.software.leovegas.drugstore.product.api.GetProductResponse
import sigma.software.leovegas.drugstore.product.api.ProductDetailsResponse
import sigma.software.leovegas.drugstore.product.api.ProductStatusDTO
import sigma.software.leovegas.drugstore.product.api.ReceiveProductResponse
import sigma.software.leovegas.drugstore.product.api.ReturnProductQuantityRequest
import sigma.software.leovegas.drugstore.product.api.ReturnProductsResponse
import sigma.software.leovegas.drugstore.product.api.SearchProductResponse


@DisplayName("ProductResource test")
@SpringBootTest(webEnvironment = RANDOM_PORT)
class ProductResourceTest @Autowired constructor(
    @LocalServerPort val port: Int,
    val restTemplate: TestRestTemplate,
    val transactionalTemplate: TransactionTemplate,
    val repository: ProductRepository,
    val objectMapper: ObjectMapper,
    val productProperties: ProductProperties
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
            repository.deleteAllInBatch()
        }

        // given
        val created = transactionalTemplate.execute {
            repository.save(
                Product(
                    name = "A test product",
                    price = BigDecimal("1.23"),
                    quantity = 3,
                )
            )
        }.get()

        // and
        val productNumber = created.id

        // when
        val response = restTemplate.exchange(
            "$baseUrl/api/v1/products/{productNumber}/price",
            GET, null, respTypeRef<BigDecimal>(), productNumber
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        // and
        val price = response.body
        assertThat(price).isEqualTo(BigDecimal("1.23"))
    }

    @Test
    fun `should create product`() {

        // given
        val httpEntity = HttpEntity(
            listOf(
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
        )

        // when
        val response = restTemplate
            .exchange("$baseUrl/api/v1/products", POST, httpEntity, respTypeRef<List<CreateProductResponse>>())

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)

        // and
        val body = response.body.get("body")
        assertThat(body).hasSize(2)
        assertThat(body[0].createdAt).isBeforeOrEqualTo(LocalDateTime.now())
        assertThat(body[0].status).isEqualTo(ProductStatusDTO.CREATED)
    }

    @Test
    fun `should receive product`() {

        // given
        val saved = transactionalTemplate.execute {
            repository.save(
                Product(
                    name = "test",
                    quantity = 10,
                    status = ProductStatus.CREATED
                )
            )
        }.get()

        val httpEntity = HttpEntity(listOf(saved.id))

        // when
        val response = restTemplate
            .exchange(
                "$baseUrl/api/v1/products/receive",
                PUT,
                httpEntity,
                respTypeRef<List<ReceiveProductResponse>>()
            )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.ACCEPTED)

        // and
        val body = response.body.get("body")
        assertThat(body).hasSize(1)
        assertThat(body[0].status).isEqualTo(ProductStatusDTO.RECEIVED)
    }

    @Test
    fun `should get products details by ids`() {

        // given
        transactionalTemplate.execute {
            repository.deleteAll()
        }

        // and
        val ids = transactionalTemplate.execute {
            repository.saveAll(
                listOf(
                    Product(
                        name = "test1",
                        price = BigDecimal("20.00"),
                        quantity = 1
                    ),
                    Product(
                        name = "test2",
                        price = BigDecimal("30.00"),
                        quantity = 2
                    )
                )
            ).map { it.id }
        }.get()

        // when
        val response = restTemplate
            .exchange(
                "$baseUrl/api/v1/products/details?ids=${ids[0]}&ids=${ids[1]}",
                GET,
                null,
                respTypeRef<List<ProductDetailsResponse>>()
            )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        // and
        val body = response.body.get("body")
        assertThat(body).hasSize(2)
        assertThat(body[0].name).isEqualTo("test1")
        assertThat(body[0].quantity).isEqualTo(1)
        assertThat(body[0].price).isEqualTo(BigDecimal("20.00"))
        assertThat(body[1].name).isEqualTo("test2")
        assertThat(body[1].quantity).isEqualTo(2)
        assertThat(body[1].price).isEqualTo(BigDecimal("30.00"))
    }

    @Test
    fun `should get popular products`() {

        // given
        transactionalTemplate.execute {
            repository.deleteAll()
        }

        // and
        val saved = transactionalTemplate.execute {
            repository.saveAll(
                listOf(
                    Product(
                        name = "aspirin",
                        status = ProductStatus.RECEIVED,
                        quantity = 10,
                    ),
                    Product(
                        name = "aspirin2",
                        status = ProductStatus.RECEIVED,
                        quantity = 10,

                        ),
                    Product(
                        name = "mostPopular",
                        status = ProductStatus.CREATED,
                        quantity = 10,
                    ),
                    Product(
                        name = "some2",
                        status = ProductStatus.RECEIVED,
                        quantity = 10,
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
        assertThat(body[0].id).isEqualTo(saved[1].id)
        assertThat(body[0].name).isEqualTo("aspirin2")
        assertThat(body[1].id).isEqualTo(saved[0].id)
        assertThat(body[1].name).isEqualTo("aspirin")
    }

    @Test
    fun `should search products by search word sorted by price ascendant`() {

        // given
        transactionalTemplate.execute {
            repository.deleteAllInBatch()
        }

        // and

        val saved = transactionalTemplate.execute {
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
        assertThat(body[0].id).isEqualTo(saved[0].id)
        assertThat(body[0].price).isEqualTo(BigDecimal("10.00"))
        assertThat(body[1].id).isEqualTo(saved[1].id)
        assertThat(body[1].price).isEqualTo(BigDecimal("50.00"))
    }

    @Test
    fun `should search products by search word sorted by price descendant`() {

        // given
        transactionalTemplate.execute {
            repository.deleteAllInBatch()
        }

        // and

        val saved = transactionalTemplate.execute {
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
        assertThat(body[0].id).isEqualTo(saved[1].id)
        assertThat(body[0].price).isEqualTo(BigDecimal("50.00"))
        assertThat(body[1].id).isEqualTo(saved[0].id)
        assertThat(body[1].price).isEqualTo(BigDecimal("10.00"))
    }

    @Test
    fun `should search products by search word sorted by popularity descendant`() {

        // given
        transactionalTemplate.execute {
            repository.deleteAllInBatch()
        }

        // and
        val ids = transactionalTemplate.execute {
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
                        name = "aspirin",
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

        // when
        val response = restTemplate
            .exchange(
                "$baseUrl/api/v1/products/search?search=aspirin&sortField=popularity&sortDirection=DESC",
                GET,
                null,
                respTypeRef<List<SearchProductResponse>>()
            )
        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        // and
        val body = response.body.get("body")
        assertThat(body).hasSize(2)
        assertThat(body[0].id).isEqualTo(ids[1])
        assertThat(body[1].id).isEqualTo(ids[0])
    }

    @Test
    fun `should deliver products`() {

        // given
        val saved = transactionalTemplate.execute {
            repository.save(
                Product(
                    name = "test",
                    quantity = 10
                )
            )
        }.get()

        // and
        val httpEntity = HttpEntity(
            listOf(
                DeliverProductsQuantityRequest(
                    id = saved.id ?: -1,
                    quantity = 3
                )
            )
        )

        // when
        val response = restTemplate
            .exchange(
                "$baseUrl/api/v1/products/deliver",
                PUT,
                httpEntity,
                respTypeRef<List<DeliverProductsResponse>>()
            )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.ACCEPTED)

        // and
        val body = response.body.get("body")
        assertThat(body).hasSize(1)
        assertThat(body[0].quantity).isEqualTo(7)  // 10 - 3
        assertThat(body[0].updatedAt).isBeforeOrEqualTo(LocalDateTime.now())
    }

    @Test
    fun `should return products`() {

        // given
        val saved = transactionalTemplate.execute {
            repository.save(
                Product(
                    name = "test",
                    quantity = 10
                )
            )
        }.get()

        // and
        val httpEntity = HttpEntity(
            listOf(
                ReturnProductQuantityRequest(
                    id = saved.id ?: -1,
                    quantity = 3
                )
            )
        )

        // when
        val response = restTemplate
            .exchange(
                "$baseUrl/api/v1/products/return",
                PUT,
                httpEntity,
                respTypeRef<List<ReturnProductsResponse>>()
            )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.ACCEPTED)

        // and
        val body = response.body.get("body")
        assertThat(body).hasSize(1)
        assertThat(body[0].quantity).isEqualTo(13)  // 10 + 3 = 13
        assertThat(body[0].updatedAt).isBeforeOrEqualTo(LocalDateTime.now())
    }
}
