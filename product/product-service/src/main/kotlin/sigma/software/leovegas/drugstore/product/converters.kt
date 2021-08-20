package sigma.software.leovegas.drugstore.product


import sigma.software.leovegas.drugstore.product.api.ProductRequest
import sigma.software.leovegas.drugstore.product.api.ProductResponse

// CreateProductRequest <-> Product entity

fun Product.toProductResponse(): ProductResponse =
    ProductResponse(
        id = id ?: -1,
        name = name,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

fun ProductRequest.toEntity(): Product =
    Product(
        name = name,
    )

fun List<Product>.toProductResponseList(): List<ProductResponse> = this.map(Product::toProductResponse)
