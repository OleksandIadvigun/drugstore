package sigma.software.leovegas.drugstore.product

// CreateProductRequest <-> Product entity

import sigma.software.leovegas.drugstore.product.api.ProductRequest
import sigma.software.leovegas.drugstore.product.api.ProductResponse

// CreateProductRequest <-> Product entity

fun Product.toProductResponse(): ProductResponse =
    ProductResponse(
        id = id ?: -1,
        name = name,
        price = price
    )

fun ProductRequest.toEntity(): Product =
    Product(
        name = name,
        price = price
    )

fun List<Product>.toProductResponseList(): List<ProductResponse> {
    return this.map { it.toProductResponse() }.toMutableList()
}
