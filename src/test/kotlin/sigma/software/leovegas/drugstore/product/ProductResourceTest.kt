package sigma.software.leovegas.drugstore.product

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.fail
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpMethod.POST
import org.springframework.http.HttpStatus
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.infrastructure.extensions.respTypeRef
import java.math.BigDecimal
import org.springframework.boot.test.web.client.TestRestTemplate

@DisplayName("ProductResource test")
@SpringBootTest(webEnvironment = RANDOM_PORT)
class ProductResourceTest(
    @Autowired val restTemplate: TestRestTemplate,
    @Autowired val service: ProductService,
    @Autowired val transactionalTemplate: TransactionTemplate,
) {
    @Test
    fun `should create product`() {
        // given
        val httpEntity = HttpEntity(
            ProductRequest(
                name = "test product",
                price = BigDecimal.TEN,
                quantity = 5
            )
        )

        // when
        val response = restTemplate.exchange("/products", POST, httpEntity, respTypeRef<ProductResponse>())

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
            quantity = 2
        )
        val httpEntity = HttpEntity(
            ProductRequest(
                name = "test product edited",
                price = BigDecimal.TEN,
                quantity = 5
            )
        )

        // when
        val savedProduct = transactionalTemplate.execute {
            service.create(newProduct)
        } ?: kotlin.test.fail("result is expected")
        val response = restTemplate.exchange(
            "/products/${savedProduct.id}", HttpMethod.PUT, httpEntity, respTypeRef<ProductResponse>()
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.ACCEPTED)

        // and
        val body = response.body ?: fail("body may not be null")
        assertThat(body.name).isEqualTo(httpEntity.body?.name)
        assertThat(body.price).isEqualTo(httpEntity.body?.price)
        assertThat(body.quantity).isEqualTo(httpEntity.body?.quantity)
    }

    @Test
    fun `should get products`() {
        // when
        val response = restTemplate.exchange("/products", GET, null, respTypeRef<List<ProductResponse>>())

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        // and
        val body = response.body ?: fail("body may not be null")
        assertThat(body.size).isNotNull
    }

    @Test
    fun `should get ONE product`() {
        // given
        val newProduct = ProductRequest(
            name = "test",
            price = BigDecimal.TEN.setScale(2),
            quantity = 2
        )

        // when
        val savedProduct = transactionalTemplate.execute {
            service.create(newProduct)
        } ?: kotlin.test.fail("result is expected")
        val response = restTemplate.exchange(
            "/products/${savedProduct.id}", HttpMethod.GET, null, respTypeRef<ProductResponse>()
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        // and
        val body = response.body ?: fail("body may not be null")
        assertThat(body.id).isEqualTo(savedProduct.id)
        assertThat(body.name).isEqualTo(savedProduct.name)
        assertThat(body.price).isEqualTo(savedProduct.price)
        assertThat(body.quantity).isEqualTo(savedProduct.quantity)
    }

    @Test
    fun `should delete ONE product`() {
        // given
        val newProduct = ProductRequest(
            name = "test",
            price = BigDecimal.ONE,
            quantity = 2
        )

        // when
        val savedProduct = transactionalTemplate.execute {
            service.create(newProduct)
        } ?: kotlin.test.fail("result is expected")
        val response = restTemplate.exchange(
            "/products/${savedProduct.id}", HttpMethod.DELETE, null, respTypeRef<ProductResponse>()
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.NO_CONTENT)

        // and
        assertThrows<ResourceNotFoundException> {
            service.getOne(savedProduct.id!!)
        }
    }
}
