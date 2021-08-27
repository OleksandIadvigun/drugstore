package sigma.software.leovegas.drugstore.product

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.matching.ContainsPattern
import java.math.BigDecimal
import java.time.LocalDateTime
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.fail
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpMethod.POST
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.accountancy.api.PriceItemResponse
import sigma.software.leovegas.drugstore.infrastructure.extensions.respTypeRef
import sigma.software.leovegas.drugstore.product.api.ProductRequest
import sigma.software.leovegas.drugstore.product.api.ProductResponse

@AutoConfigureWireMock(port = 8082)
@DisplayName("ProductResource test")
@SpringBootTest(webEnvironment = RANDOM_PORT)
class ProductResourceTest @Autowired constructor(
    @LocalServerPort val port: Int,
    val restTemplate: TestRestTemplate,
    val service: ProductService,
    val transactionalTemplate: TransactionTemplate,
    val repository: ProductRepository,
    val objectMapper: ObjectMapper,
    val productProperties: ProductProperties
) {

    lateinit var baseUrl: String
    val wiremockAccountancyServer = WireMockServer(8084)

    @BeforeEach
    fun setup() {
        baseUrl = "http://${productProperties.host}:$port"
    }

    @Test
    fun `should create product`() {

        // given
        val httpEntity = HttpEntity(
            ProductRequest(
                name = "test product",
            )
        )

        // when
        val response =
            restTemplate.exchange("$baseUrl/api/v1/products", POST, httpEntity, respTypeRef<ProductResponse>())

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)

        // and
        val body = response.body ?: fail("body may not be null")
        assertThat(body.id).isNotNull
        assertThat(body.createdAt).isBefore(LocalDateTime.now())
    }

    @Test
    fun `should update product`() {

        // given
        val newProduct = ProductRequest(
            name = "test",
        )

        val savedProduct = transactionalTemplate.execute {
            service.create(newProduct)
        } ?: fail("result is expected")

        val httpEntity = HttpEntity(
            ProductRequest(
                name = "test product edited",
            )
        )

        // when
        val response = restTemplate.exchange(
            "$baseUrl/api/v1/products/${savedProduct.id}", HttpMethod.PUT, httpEntity, respTypeRef<ProductResponse>()
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.ACCEPTED)

        // and
        val body = response.body ?: fail("body may not be null")
        assertThat(body.name).isEqualTo(httpEntity.body?.name)
        assertThat(body.createdAt).isBefore(body.updatedAt)
    }

    @Test
    fun `should get products`() {

        // given
        transactionalTemplate.execute {
            repository.deleteAll()
        }

        // and
        val savedProducts = transactionalTemplate.execute {
            repository.saveAll(
                listOf(
                    Product(
                        name = "test",
                    ),
                    Product(
                        name = "aspirin",
                    ),
                    Product(
                        name = "bca",
                    )
                )
            )
        } ?: fail("result is expected")

        // and
        stubFor(
            get("/api/v1/orders/total-buys")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(
                    aResponse()
                        .withBody(
                            objectMapper
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(mapOf(savedProducts[1].id to 5, savedProducts[0].id to 3))
                        )
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                )
        )

        // when
        val response = restTemplate.exchange(
            "$baseUrl/api/v1/products",
            GET,
            null,
            respTypeRef<RestResponsePage<ProductResponse>>()
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        // and
        val body = response.body ?: fail("body may not be null")
        assertThat(body.content.size).isEqualTo(3)
        assertThat(body.content[0].totalBuys).isEqualTo(5)
        assertThat(body.content[1].totalBuys).isEqualTo(3)
        assertThat(body.content[2].totalBuys).isEqualTo(0)
    }

    @Test
    fun `should get products sorted by name ascendant`() {

        // given
        wiremockAccountancyServer.start()

        // and
        transactionalTemplate.execute {
            repository.deleteAll()
        }

        // and
        val savedProducts = transactionalTemplate.execute {
            repository.saveAll(
                listOf(
                    Product(
                        name = "test",
                    ),
                    Product(
                        name = "aspirin",
                    ),
                    Product(
                        name = "bca",
                    )
                )
            )
        } ?: fail("result is expected")

        // and
        wiremockAccountancyServer.stubFor(
            get("/api/v1/accountancy/price-by-product-ids/ids=${savedProducts[1].id},${savedProducts[2].id}," +
                    "${savedProducts[0].id}&markup=true")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(
                    aResponse()
                        .withBody(
                            objectMapper
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(
                                    listOf(
                                        PriceItemResponse(
                                            productId = savedProducts[0].id ?: -1,
                                            price = BigDecimal("20.00")
                                        ),
                                        PriceItemResponse(
                                            productId = savedProducts[2].id ?: -1,
                                            price = BigDecimal("40.00"),
                                        ),
                                        PriceItemResponse(
                                            productId = savedProducts[1].id ?: -1,
                                            price = BigDecimal("25.00")
                                        )
                                    )
                                )
                        )
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                )
        )

        // when
        val response = restTemplate.exchange(
            "$baseUrl/api/v1/products?sortField=name&sortDirection=ASC",
            GET,
            null,
            respTypeRef<RestResponsePage<ProductResponse>>()
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        // and
        val body = response.body ?: fail("body may not be null")
        assertThat(body.content.size).isEqualTo(3)
        assertThat(body.content[0].name).isEqualTo("aspirin")
        assertThat(body.content[1].name).isEqualTo("bca")
        assertThat(body.content[2].name).isEqualTo("test")

        // and
        wiremockAccountancyServer.stop()
    }

    @Test
    fun `should get products sorted by creation date ascendant`() {

        // given
        wiremockAccountancyServer.start()

        // and
        transactionalTemplate.execute {
            repository.deleteAll()
        }

        // and
        val savedProduct1 = transactionalTemplate.execute {
            repository.save(Product(name = "test"))
        } ?: fail("result is expected")

        // and
        val savedProduct2 = transactionalTemplate.execute {
            repository.save(Product(name = "aspirin"))
        } ?: fail("result is expected")

        // and
        wiremockAccountancyServer.stubFor(
            get("/api/v1/accountancy/price-by-product-ids/ids=${savedProduct1.id},${savedProduct2.id}&markup=true")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(
                    aResponse()
                        .withBody(
                            objectMapper
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(
                                    listOf(
                                        PriceItemResponse(
                                            productId = savedProduct1.id ?: -1,
                                            price = BigDecimal("20.00")
                                        ),
                                        PriceItemResponse(
                                            productId = savedProduct2.id ?: -1,
                                            price = BigDecimal("40.00"),
                                        )
                                    )
                                )
                        )
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                )
        )

        // when
        val response = restTemplate.exchange(
            "$baseUrl/api/v1/products?sortField=createdAt&sortDirection=ASC",
            GET,
            null,
            respTypeRef<RestResponsePage<ProductResponse>>()
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        // and
        val body = response.body ?: fail("body may not be null")
        assertThat(body.content.size).isEqualTo(2)
        assertThat(body.content[0].name).isEqualTo("test")
        assertThat(body.content[1].name).isEqualTo("aspirin")

        // and
        wiremockAccountancyServer.stop()
    }

    @Test
    fun `should get products sorted by price descendant`() {

        // given
        wiremockAccountancyServer.start()

        // and
        transactionalTemplate.execute {
            repository.deleteAll()
        }

        // and
        val savedProducts = transactionalTemplate.execute {
            repository.saveAll(
                listOf(
                    Product(
                        name = "test",
                    ),
                    Product(
                        name = "aspirin",
                    ),
                    Product(
                        name = "bca",
                    )
                )
            )
        } ?: fail("result is expected")

        // and
        wiremockAccountancyServer.stubFor(
            get("/api/v1/accountancy/price-by-product-ids/ids=${savedProducts[0].id},${savedProducts[1].id}," +
                    "${savedProducts[2].id}&markup=true")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(
                    aResponse()
                        .withBody(
                            objectMapper
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(
                                    listOf(
                                        PriceItemResponse(
                                            productId = savedProducts[0].id ?: -1,
                                            price = BigDecimal("20.00")
                                        ),
                                        PriceItemResponse(
                                            productId = savedProducts[2].id ?: -1,
                                            price = BigDecimal("40.00"),
                                        ),
                                        PriceItemResponse(
                                            productId = savedProducts[1].id ?: -1,
                                            price = BigDecimal("25.00")
                                        )
                                    )
                                )
                        )
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                )
        )

        // when
        val response = restTemplate.exchange(
            "$baseUrl/api/v1/products?sortField=price&sortDirection=DESC",
            GET,
            null,
            respTypeRef<RestResponsePage<ProductResponse>>()
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        // and
        val body = response.body ?: fail("body may not be null")
        assertThat(body.content.size).isEqualTo(3)
        assertThat(body.content[0].name).isEqualTo("bca")
        assertThat(body.content[1].name).isEqualTo("aspirin")
        assertThat(body.content[2].name).isEqualTo("test")

        // and
        wiremockAccountancyServer.stop()
    }

    @Test
    fun `should get products by search word`() {

        // given
        wiremockAccountancyServer.start()

        // and
        transactionalTemplate.execute {
            repository.deleteAll()
        }

        // and
        val savedProducts = transactionalTemplate.execute {
            repository.saveAll(
                listOf(
                    Product(
                        name = "test",
                    ),
                    Product(
                        name = "aspirin",
                    ),
                    Product(
                        name = "bca",
                    )
                )
            )
        } ?: fail("result is expected")

        // and
        stubFor(
            get("/api/v1/orders/total-buys")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(
                    aResponse()
                        .withBody(
                            objectMapper
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(mapOf(savedProducts[1].id to 5))
                        )
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                )
        )

        // when
        val response = restTemplate.exchange(
            "$baseUrl/api/v1/products?search=aspirin",
            GET,
            null,
            respTypeRef<RestResponsePage<ProductResponse>>()
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        // and
        val body = response.body ?: fail("body may not be null")
        assertThat(body.content.size).isEqualTo(1)
        assertThat(body.content[0].name).isEqualTo("aspirin")

        // and
        wiremockAccountancyServer.stop()
    }

    @Test
    fun `should get products by search word sorted by price`() {

        // given
        wiremockAccountancyServer.start()

        // and
        transactionalTemplate.execute {
            repository.deleteAll()
        }

        // and
        val savedProducts = transactionalTemplate.execute {
            repository.saveAll(
                listOf(
                    Product(
                        name = "test",
                    ),
                    Product(
                        name = "aspirin",
                    ),
                    Product(
                        name = "CCC",
                    )
                )
            )
        } ?: fail("result is expected")

        wiremockAccountancyServer.stubFor(
            get("/api/v1/accountancy/price-by-product-ids/ids=${savedProducts[1].id}&markup=true")
                .withHeader("Content-Type", ContainsPattern(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(
                    aResponse()
                        .withBody(
                            objectMapper
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(
                                    listOf(
                                        PriceItemResponse(
                                            productId = savedProducts[1].id ?: -1,
                                            price = BigDecimal("20.00")
                                        ),
                                        PriceItemResponse(
                                            productId = savedProducts[1].id ?: -1,
                                            price = BigDecimal("40.00"),
                                        ),
                                        PriceItemResponse(
                                            productId = savedProducts[1].id ?: -1,
                                            price = BigDecimal("25.00")
                                        )
                                    )
                                )
                        )
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                )
        )

        // when
        val response = restTemplate.exchange(
            "$baseUrl/api/v1/products?search=aspirin&sortField=price",
            GET,
            null,
            respTypeRef<RestResponsePage<ProductResponse>>()
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        // and
        val body = response.body ?: fail("body may not be null")
        assertThat(body.content.size).isEqualTo(3)
        assertThat(body.content[0].name).isEqualTo("aspirin")
        assertThat(body.content[0].price).isEqualTo(BigDecimal("40.00"))
        assertThat(body.content[1].name).isEqualTo("aspirin")
        assertThat(body.content[1].price).isEqualTo(BigDecimal("25.00"))
        assertThat(body.content[2].name).isEqualTo("aspirin")
        assertThat(body.content[2].price).isEqualTo(BigDecimal("20.00"))

        // and
        wiremockAccountancyServer.stop()
    }

    @Test
    fun `should get products by ids`() {

        // given
        transactionalTemplate.execute {
            repository.deleteAllInBatch()
        }

        // and
        val ids = transactionalTemplate.execute {
            repository.saveAll(
                listOf(
                    Product(
                        name = "test1",
                    ),
                    Product(
                        name = "test2",
                    )
                )
            ).map { it.id ?: -1 }.toList()
        } ?: listOf(-1L)

        // when
        val response = restTemplate
            .exchange(
                "$baseUrl/api/v1/products-by-ids/?ids=${ids[0]}&ids=${ids[1]}",
                GET,
                null,
                respTypeRef<List<ProductResponse>>()
            )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        val body = response.body ?: fail("body may not be null")
        assertThat(body).isNotNull
        assertThat(body).hasSize(2)
        assertThat(body[0].id).isEqualTo(ids[0])
        assertThat(body[1].id).isEqualTo(ids[1])
    }

    @Test
    fun `should get product by id`() {

        // given
        val newProduct = ProductRequest(
            name = "test",
        )

        // when
        val savedProduct = transactionalTemplate.execute {
            service.create(newProduct)
        } ?: fail("result is expected")
        val response = restTemplate.exchange(
            "$baseUrl/api/v1/products/${savedProduct.id}", GET, null, respTypeRef<ProductResponse>()
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        // and
        val body = response.body ?: fail("body may not be null")
        assertThat(body.id).isEqualTo(savedProduct.id)
        assertThat(body.name).isEqualTo(savedProduct.name)
        assertThat(body.createdAt).isBefore(LocalDateTime.now())  //todo should to be before updatedAt
    }

    @Test
    fun `should delete product by id`() {

        // given
        val newProduct = ProductRequest(
            name = "test",
        )

        // when
        val savedProduct = transactionalTemplate.execute {
            service.create(newProduct)
        } ?: fail("result is expected")
        val response = restTemplate.exchange(
            "$baseUrl/api/v1/products/${savedProduct.id}", HttpMethod.DELETE, null, respTypeRef<ProductResponse>()
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.NO_CONTENT)

        // and
        assertThrows<ResourceNotFoundException> {
            service.getOne(savedProduct.id)
        }
    }
}
