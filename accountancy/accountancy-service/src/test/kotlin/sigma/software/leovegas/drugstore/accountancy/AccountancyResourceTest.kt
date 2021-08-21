package sigma.software.leovegas.drugstore.accountancy

import java.math.BigDecimal
import java.time.LocalDateTime
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpMethod.POST
import org.springframework.http.HttpStatus
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.accountancy.api.PriceItemRequest
import sigma.software.leovegas.drugstore.accountancy.api.PriceItemResponse
import sigma.software.leovegas.drugstore.accountancy.client.AccountancyProperties
import sigma.software.leovegas.drugstore.extensions.respTypeRef

@DisplayName("Accountancy Resource test")
@SpringBootTest(webEnvironment = RANDOM_PORT)
class AccountancyResourceTest @Autowired constructor(
    @LocalServerPort val port: Int,
    val restTemplate: TestRestTemplate,
    val service: AccountancyService,
    val transactionalTemplate: TransactionTemplate,
    val repository: PriceItemRepository,
    val accountancyProperties: AccountancyProperties
) {

    lateinit var baseUrl: String

    @BeforeEach
    fun setup() {
        baseUrl = "http://${accountancyProperties.host}:$port"
    }

    @Test
    fun `should create price item`() {

        // given
        val httpEntity = HttpEntity(
            PriceItemRequest(
                productId = 1L,
                price = BigDecimal.TEN,
            )
        )

        // when
        val response = restTemplate.exchange(
            "$baseUrl/api/v1/accountancy/price-item",
            POST,
            httpEntity,
            respTypeRef<PriceItemResponse>()
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)

        // and
        val body = response.body ?: fail("body may not be null")
        assertThat(body.id).isNotNull
        assertThat(body.price).isEqualTo(BigDecimal.TEN)
        assertThat(body.createdAt).isBefore(LocalDateTime.now())
    }

    @Test
    fun `should update price item`() {

        // given
        val priceItem = PriceItem(
            productId = 1L,
            price = BigDecimal.ONE,
        )

        val savedPriceItem = transactionalTemplate.execute {
            repository.save(priceItem)
        } ?: fail("result is expected")

        val httpEntity = HttpEntity(
            PriceItemRequest(
                productId = 1L,
                price = BigDecimal.TEN,
            )
        )

        // when
        val response = restTemplate.exchange(
            "$baseUrl/api/v1/accountancy/price-item/${savedPriceItem.id}",
            HttpMethod.PUT,
            httpEntity,
            respTypeRef<PriceItemResponse>()
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.ACCEPTED)

        // and
        val body = response.body ?: fail("body may not be null")
        assertThat(body.price).isEqualTo(httpEntity.body?.price)
        assertThat(body.createdAt).isBefore(body.updatedAt)
    }

    @Test
    fun `should get products price`() {

        // given
        transactionalTemplate.execute {
            repository.deleteAll()
        } ?: fail("result is expected")

        // and
        val priceItem = PriceItem(
            productId = 1L,
            price = BigDecimal("10.00"),
        )

        // and
        val savedPriceItem = transactionalTemplate.execute {
            repository.save(priceItem)
        } ?: fail("result is expected")

        // when
        val response = restTemplate.exchange(
            "$baseUrl/api/v1/accountancy/product-price",
            HttpMethod.GET,
            null,
            respTypeRef<Map<Long?, BigDecimal>>()
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        // and
        val body = response.body ?: fail("body may not be null")
        assertThat(body.size).isEqualTo(1)
        assertThat(body[1L]).isEqualTo(BigDecimal("10.00"))
    }

    @Test
    fun `should get products price by products ids`() {

        // given
        transactionalTemplate.execute {
            repository.deleteAll()
        } ?: fail("result is expected")

        // and
        val priceItemRequest = PriceItemRequest(
            productId = 1L,
            price = BigDecimal.ONE,
        )

        val savedPriceItem = transactionalTemplate.execute {
            repository.saveAll(
                listOf(
                    PriceItem(
                        productId = 1L,
                        price = BigDecimal.ONE,
                    ),
                    PriceItem(
                        productId = 2L,
                        price = BigDecimal.ONE,
                    ),
                    PriceItem(
                        productId = 3L,
                        price = BigDecimal.ONE,
                    )
                )
            )
        } ?: fail("result is expected")

        // when
        val response = restTemplate.exchange(
            "$baseUrl/api/v1/accountancy/product-price-by-ids?ids=1,2",
            HttpMethod.GET,
            null,
            respTypeRef<Map<Long?, BigDecimal>>()
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        // and
        val body = response.body ?: fail("body may not be null")
        assertThat(body.size).isEqualTo(2)
        assertThat(body[1]).isEqualTo(BigDecimal("1.00"))
        assertThat(body[2]).isEqualTo(BigDecimal("1.00"))
    }
}
