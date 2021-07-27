package sigma.software.leovegas.drugstore.service

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import sigma.software.leovegas.drugstore.dto.ProductRequest
import sigma.software.leovegas.drugstore.dto.ProductResponse
import sigma.software.leovegas.drugstore.persistence.entity.Product
import sigma.software.leovegas.drugstore.persistence.entity.convertToProduct
import sigma.software.leovegas.drugstore.persistence.entity.convertToProductResponse
import sigma.software.leovegas.drugstore.persistence.entity.convertToProductResponseList


class ProductFunctionsTest {
    var product = Product(name = "abdula")
    var productResponse = ProductResponse(name = "abdula")
    var productRequest = ProductRequest(name = "abdula")

    @Test
    fun `should convert to ProductResponse` () {
        val actual = product.convertToProductResponse()

        Assertions.assertEquals(productResponse, actual)
    }

    @Test
    fun `should convert to Product` () {
        val actual = productRequest.convertToProduct()

        Assertions.assertEquals(product, actual)
    }

    @Test
    fun `should return MutableList Of ProductsResponse`() {
        val mutableList = mutableListOf(product, product)
        val mutableListOfDto = mutableListOf(productResponse, productResponse)

        val actual = mutableList.convertToProductResponseList()

        Assertions.assertEquals(mutableListOfDto, actual)
    }
}
