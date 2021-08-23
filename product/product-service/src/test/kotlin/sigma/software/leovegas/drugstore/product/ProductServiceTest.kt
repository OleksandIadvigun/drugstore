package sigma.software.leovegas.drugstore.product

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.matching.ContainsPattern
import java.time.LocalDateTime
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.product.api.ProductRequest
import sigma.software.leovegas.drugstore.product.api.ProductResponse

@AutoConfigureTestDatabase
@AutoConfigureWireMock(port = 8082)
@DisplayName("ProductService test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductServiceTest @Autowired constructor(
    val service: ProductService,
    val transactionTemplate: TransactionTemplate,
    val repository: ProductRepository,
    val objectMapper: ObjectMapper
) {

    @Test
    fun `should get products`() {

        // given
        transactionTemplate.execute {
            repository.deleteAllInBatch()
        }

        // and
        var saved = mutableListOf<Product>()
        transactionTemplate.execute {
            saved = repository.saveAll(
                listOf(
                    Product(
                        name = "aspirin",
                    ),
                    Product(
                        name = "test2",
                    ),
                    Product(
                        name = "some",
                    ),
                    Product(
                        name = "some2",
                    )
                )
            )
        }

        //and
        val responseExpected = mapOf<Long, Int>(saved[0].id!! to 5, saved[1].id!! to 2, saved[2].id!! to 9)

        // and
        stubFor(
            get("/api/v1/orders/total-buys")
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
        val all = service.getAll(0, 5, "", "default", "DESC")

        // then
        assertThat(all).isNotNull
        assertThat(all.totalElements).isEqualTo(4)
        assertThat(all.content[0].totalBuys).isEqualTo(9)
        assertThat(all.content[1].totalBuys).isEqualTo(5)
        assertThat(all.content[2].totalBuys).isEqualTo(2)
        assertThat(all.content[3].totalBuys).isEqualTo(0)
    }

    @Test
    fun `should get products by ids`() {

        // given
        transactionTemplate.execute {
            repository.deleteAllInBatch()
        }

        // and
        val ids = transactionTemplate.execute {
            repository.saveAll(
                listOf(
                    Product(
                        name = "test1",
                    ),
                    Product(
                        name = "test2",
                    )
                )
            ).map { it.id }
        }

        // when
        val products = service.getProductsByIds(ids as List<Long>)

        // then
        assertThat(products).hasSize(2)
        assertThat(products[0].name).isEqualTo("test1")
        assertThat(products[1].name).isEqualTo("test2")
    }

    @Test
    fun `should get product by id`() {

        // given
        val productRequest = ProductRequest(
            name = "test",
        )

        // and
        val saved = transactionTemplate.execute {
            repository.save(productRequest.toEntity()).toProductResponse()
        } ?: fail("result is expected")

        // when
        val actual = service.getOne(saved.id)

        // then
        assertThat(actual).isNotNull
        assertThat(actual.id).isEqualTo(saved.id)
        assertThat(actual.name).isEqualTo(saved.name)
    }

    @Test
    fun `should not get not exist product`() {

        // given
        val id = Long.MAX_VALUE

        // when
        val exception = assertThrows<ResourceNotFoundException> {
            service.getOne(id)
        }

        //then
        assertThat(exception.message).isEqualTo("This product with id: $id doesn't exist!")
    }

    @Test
    fun `should create product`() {

        // given
        val productRequest = ProductRequest(
            name = "test",
        )

        //and
        val productResponse = ProductResponse(
            name = "test",
        )

        // when
        val actual = service.create(productRequest)

        // then
        assertThat(actual).isNotNull
        assertThat(actual.id).isNotNull
        assertThat(productResponse.name).isEqualTo(actual.name)
        assertThat(actual.createdAt).isBeforeOrEqualTo(LocalDateTime.now())
    }

    @Test
    fun `should update product`() {

        // given
        val productRequest = ProductRequest(
            name = "test",
        )

        // and
        val saved = transactionTemplate.execute {
            repository.save(productRequest.toEntity()).toProductResponse()
        } ?: fail("result is expected")

        // and
        val randomName = Math.random().toString()
        val updatedProductRequest = ProductRequest(
            name = randomName,
        )

        // when
        val actual = service.update(saved.id, updatedProductRequest)

        // then
        assertThat(actual).isNotNull
        assertThat(randomName).isEqualTo(actual.name)
        assertThat(actual.createdAt).isBefore(actual.updatedAt)
    }

    @Test
    fun `should not update not existing product`() {

        // given
        val id = Long.MAX_VALUE

        // and
        val productRequest = ProductRequest(
            name = "test",
        )

        // when
        val exception = assertThrows<ResourceNotFoundException> {
            service.update(Long.MAX_VALUE, productRequest)
        }

        // then
        assertThat(exception.message).isEqualTo("This product with id: $id doesn't exist!")
    }

    @Test
    fun `should delete product`() {

        // given
        val productRequest = ProductRequest(
            name = "test",
        )

        // and
        val product = service.create(productRequest)

        // when
        service.delete(product.id)

        // then
        val exception = assertThrows<ResourceNotFoundException> {
            service.getOne(product.id)
        }

        //and
        assertThat(exception.message).isEqualTo("This product with id: ${product.id} doesn't exist!")
    }
}
