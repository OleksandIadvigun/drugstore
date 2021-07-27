package sigma.software.leovegas.drugstore.persistence.entity

import sigma.software.leovegas.drugstore.dto.ProductRequest
import sigma.software.leovegas.drugstore.dto.ProductResponse

fun Product.convertToProductResponse(): ProductResponse {
    val productDto = ProductResponse()
    productDto.id = id
    productDto.name = name
    productDto.price = price
    productDto.quantity = quantity
    return productDto
}

fun ProductRequest.convertToProduct(): Product {
    val product = Product()
    product.id = id
    product.name = name
    product.price = price
    product.quantity = quantity
    return product
}

fun MutableList<Product>.convertToProductResponseList(): MutableList<ProductResponse> {
    return this.map { it.convertToProductResponse() }.toMutableList()
}
