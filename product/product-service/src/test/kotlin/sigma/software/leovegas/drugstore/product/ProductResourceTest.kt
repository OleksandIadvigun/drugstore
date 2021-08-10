package sigma.software.leovegas.drugstore.product

import java.math.BigDecimal
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.fail
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpMethod.POST
import org.springframework.http.HttpStatus
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.infrastructure.extensions.respTypeRef
import sigma.software.leovegas.drugstore.product.api.ProductRequest
import sigma.software.leovegas.drugstore.product.api.ProductResponse

@DisplayName("ProductResource test")
@SpringBootTest(webEnvironment = RANDOM_PORT)
class ProductResourceTest(
    @Autowired val restTemplate: TestRestTemplate,
    @Autowired val service: ProductService,
    @Autowired val transactionalTemplate: TransactionTemplate,
    @Autowired val repository: ProductRepository,
) {

    @Test
    fun `should create product`() {

        // given
        val httpEntity = HttpEntity(
            ProductRequest(
                name = "test product",
                price = BigDecimal.TEN,
            )
        )

        // when
        val response = restTemplate.exchange("/api/v1/products", POST, httpEntity, respTypeRef<ProductResponse>())

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)

        // and
        val body = response.body ?: fail("body may not be null")
        assertThat(body.id).isNotNull
        assertThat(body.price).isEqualTo(BigDecimal.TEN)
    }

    @Test
    fun `should update product`() {

        // given
        val newProduct = ProductRequest(
            name = "test",
            price = BigDecimal.ONE,
        )

        val savedProduct = transactionalTemplate.execute {
            service.create(newProduct)
        } ?: fail("result is expected")

        val httpEntity = HttpEntity(
            ProductRequest(
                name = "test product edited",
                price = BigDecimal.TEN,
            )
        )

        // when
        val response = restTemplate.exchange(
            "/api/v1/products/${savedProduct.id}", HttpMethod.PUT, httpEntity, respTypeRef<ProductResponse>()
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.ACCEPTED)

        // and
        val body = response.body ?: fail("body may not be null")
        assertThat(body.name).isEqualTo(httpEntity.body?.name)
        assertThat(body.price).isEqualTo(httpEntity.body?.price)
    }

    @Test
    fun `should get products`() {

        // given
        transactionalTemplate.execute {
            repository.deleteAll()
        }

        // and
        transactionalTemplate.execute {
            repository.saveAll(
                listOf(
                    Product(
                        name = "test1",
                        price = BigDecimal("20.00")
                    ),
                    Product(
                        name = "test2",
                        price = BigDecimal("40.00")
                    )
                )
            )
        }

        // when
        val response = restTemplate.exchange("/api/v1/products", GET, null, respTypeRef<List<ProductResponse>>())

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        // and
        val body = response.body ?: fail("body may not be null")
        assertThat(body).hasSize(2)
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
                        price = BigDecimal("20.00")
                    ),
                    Product(
                        name = "test2",
                        price = BigDecimal("40.00")
                    )
                )
            ).map { it.id ?: -1 }.toList()
        } ?: listOf(-1L)

        // when
        val response = restTemplate
            .exchange(
                "/api/v1/products-by-ids/?ids=${ids[0]}&ids=${ids[1]}",
                GET,
                null,
                respTypeRef<List<ProductResponse>>()
            )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        val bodyByIds = response.body ?: fail("body may not be null")
        assertThat(bodyByIds).isNotNull
        assertThat(bodyByIds).hasSize(2)
        assertThat(bodyByIds[0].id).isEqualTo(ids[0])
        assertThat(bodyByIds[1].id).isEqualTo(ids[1])
    }

    @Test
    fun `should get ONE product`() {

        // given
        val newProduct = ProductRequest(
            name = "test",
            price = BigDecimal.TEN.setScale(2),
        )

        // when
        val savedProduct = transactionalTemplate.execute {
            service.create(newProduct)
        } ?: fail("result is expected")
        val response = restTemplate.exchange(
            "/api/v1/products/${savedProduct.id}", GET, null, respTypeRef<ProductResponse>()
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        // and
        val body = response.body ?: fail("body may not be null")
        assertThat(body.id).isEqualTo(savedProduct.id)
        assertThat(body.name).isEqualTo(savedProduct.name)
        assertThat(body.price).isEqualTo(savedProduct.price)
    }

    @Test
    fun `should delete ONE product`() {

        // given
        val newProduct = ProductRequest(
            name = "test",
            price = BigDecimal.ONE,
        )

        // when
        val savedProduct = transactionalTemplate.execute {
            service.create(newProduct)
        } ?: fail("result is expected")
        val response = restTemplate.exchange(
            "/api/v1/products/${savedProduct.id}", HttpMethod.DELETE, null, respTypeRef<ProductResponse>()
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.NO_CONTENT)

        // and
        assertThrows<ResourceNotFoundException> {
            service.getOne(savedProduct.id)
        }
    }
}
