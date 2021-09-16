package sigma.software.leovegas.drugstore.product.restdoc

import java.math.BigDecimal
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.infrastructure.extensions.get
import sigma.software.leovegas.drugstore.product.Product
import sigma.software.leovegas.drugstore.product.ProductProperties
import sigma.software.leovegas.drugstore.product.ProductRepository
import sigma.software.leovegas.drugstore.product.ProductStatus

@DisplayName("Get products details by product numbers REST API Doc test")
class RestApiDocGetProductsDetailsByProductNumbersTest @Autowired constructor(
    @LocalServerPort val port: Int,
    val transactionTemplate: TransactionTemplate,
    val productRepository: ProductRepository,
    val productProperties: ProductProperties,
) : RestApiDocumentationTest(productProperties) {


    @Test
    fun `should get products details by product numbers`() {

        // given
        transactionTemplate.execute {
            productRepository.deleteAll()
        }

        // given
        val productNumbers = transactionTemplate.execute {
            productRepository.saveAll(
                listOf(
                    Product(
                        productNumber = "1",
                        name = "test1",
                        price = BigDecimal("20.00"),
                        quantity = 5,
                        status = ProductStatus.RECEIVED,
                    ),
                    Product(
                        productNumber = "2",
                        name = "test2",
                        price = BigDecimal("20.00"),
                        quantity = 3,
                        status = ProductStatus.RECEIVED,
                    )
                )
            ).map { it.productNumber }
        }.get()

        of("get-products-details-by-product-numbers").`when`()
            .get("http://${productProperties.host}:$port/api/v1/products/details?" +
                    "productNumbers=${productNumbers[0]}&productNumbers=${productNumbers[1]}")
            .then()
            .assertThat().statusCode(200)
            .assertThat().body("size()", `is`(2))
            .assertThat().body("[0].name", equalTo("test1"))
            .assertThat().body("[0].quantity", equalTo(5))
            .assertThat().body("[0].price", equalTo(20.0F))
    }
}
