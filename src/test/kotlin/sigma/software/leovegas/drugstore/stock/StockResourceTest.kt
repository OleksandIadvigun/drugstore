package sigma.software.leovegas.drugstore.stock

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.fail
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.infrastructure.extensions.respTypeRef
import sigma.software.leovegas.drugstore.product.ProductRequest
import sigma.software.leovegas.drugstore.product.ProductService
import java.math.BigDecimal


@DisplayName("StockResource test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class StockResourceTest(
    @Autowired val restTemplate: TestRestTemplate,
    @Autowired val productService: ProductService,
    @Autowired val stockService: StockService,
    @Autowired val transactionalTemplate: TransactionTemplate,
) {

    @Test
    fun `should create stock`() {

        // given
        val savedProduct = transactionalTemplate.execute {
            productService.create(ProductRequest(
                name = "test",
                price = BigDecimal.TEN.setScale(2),
            ))
        }

        // and
        val httpEntity = HttpEntity(
            StockRequest(
                productId = savedProduct?.id,
                quantity = 10,
            )
        )

        // when
        val response = restTemplate.exchange(
            "/api/v1/stocks",
            HttpMethod.POST, httpEntity, respTypeRef<StockResponse>()
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)

        // and
        val body = response.body ?: fail("body may not be null")
        assertThat(body.id).isNotNull
        assertThat(body.quantity).isEqualTo(httpEntity.body?.quantity)
    }

    @Test
    fun `should update stock`() {

        // given
        val savedProduct = transactionalTemplate.execute {
            productService.create(ProductRequest(
                name = "test",
                price = BigDecimal.TEN.setScale(2),
            ))
        }

        // and
        val savedStock = transactionalTemplate.execute {
            stockService.create(
                StockRequest(
                    productId = savedProduct?.id,
                    quantity = 2,
                )
            )
        }

        // and
        val httpEntity = HttpEntity(
            StockRequest(
                productId = savedProduct?.id,
                quantity = 10,
            )
        )

        // when
        val response = restTemplate.exchange(
            "/api/v1/stocks/${savedStock?.id}",
            HttpMethod.PUT, httpEntity, respTypeRef<StockResponse>()
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.ACCEPTED)

        // and
        val body = response.body ?: fail("body may not be null")
        assertThat(body.id).isNotNull
        assertThat(body.quantity).isEqualTo(httpEntity.body?.quantity)
    }

    @Test
    fun `should return stocks`(){

        // given
        val savedProduct = transactionalTemplate.execute {
            productService.create(ProductRequest(
                name = "test",
                price = BigDecimal.TEN.setScale(2),
            ))
        }

        // and
        val savedStock = transactionalTemplate.execute {
            stockService.create(
                StockRequest(
                    productId = savedProduct?.id,
                    quantity = 2,
                )
            )
        }

        // when
        val response = restTemplate.exchange(
            "/api/v1/stocks",
            HttpMethod.GET, null, respTypeRef<List<StockResponse>>()
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        // and
        val body = response.body ?: fail("body may not be null")
        assertTrue(body.isNotEmpty())
    }

    @Test
    fun `should return ONE stock`(){

        // given
        val savedProduct = transactionalTemplate.execute {
            productService.create(ProductRequest(
                name = "test",
                price = BigDecimal.TEN.setScale(2),
            ))
        }

        // and
        val savedStock = transactionalTemplate.execute {
            stockService.create(
                StockRequest(
                    productId = savedProduct?.id,
                    quantity = 2,
                )
            )
        }

        // when
        val response = restTemplate.exchange(
            "/api/v1/stocks/${savedStock?.id}",
            HttpMethod.GET, null, respTypeRef<StockResponse>()
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        // and
        val body = response.body ?: fail("body may not be null")
        assertThat(body.quantity).isEqualTo(savedStock?.quantity)
    }

    @Test
    fun `should delete stock`(){

        // given
        val savedProduct = transactionalTemplate.execute {
            productService.create(ProductRequest(
                name = "test",
                price = BigDecimal.TEN.setScale(2),
            ))
        }

        // and
        val savedStock = transactionalTemplate.execute {
            stockService.create(
                StockRequest(
                    productId = savedProduct?.id,
                    quantity = 2,
                )
            )
        }

        // when
        val response = restTemplate.exchange(
            "/api/v1/stocks/${savedStock?.id}",
            HttpMethod.DELETE, null, respTypeRef<StockResponse>()
        )

        // then
        assertThrows<StockNotFoundException> {
            transactionalTemplate.execute {
                stockService.getOne(savedStock?.id!!)
            }
        }
    }
}

