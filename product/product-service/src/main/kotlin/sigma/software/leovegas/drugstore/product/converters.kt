package sigma.software.leovegas.drugstore.product

import sigma.software.leovegas.drugstore.product.api.CreateProductRequest
import sigma.software.leovegas.drugstore.product.api.CreateProductResponse
import sigma.software.leovegas.drugstore.product.api.GetProductResponse
import sigma.software.leovegas.drugstore.product.api.ProductStatusDTO
import sigma.software.leovegas.drugstore.product.api.SearchProductResponse

// CreateProductRequest <-> Product entity

fun CreateProductRequest.toEntity() = Product(
    productNumber = productNumber,
    name = name,
    quantity = quantity,
    price = price,
)

fun List<CreateProductRequest>.toEntityList() = this.map(CreateProductRequest::toEntity)

fun Product.toCreateProductResponse() =
    CreateProductResponse(
        productNumber = productNumber,
        status = status.toDTO(),
        name = name,
        quantity = quantity,
        price = price,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

fun List<Product>.toCreateProductResponseList() = this.map(Product::toCreateProductResponse)

// Product entity -> GetProductResponse

fun Product.toGetProductResponse() =
    GetProductResponse(
        productNumber = productNumber,
        name = name
    )

// Product entity -> SearchProductResponse

fun Product.toSearchProductResponse() =
    SearchProductResponse(
        productNumber = productNumber,
        name = name,
        quantity = quantity,
        price = price,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

fun List<Product>.toSearchProductResponseList() = this.map(Product::toSearchProductResponse)

// ProductStatus -> ProductStatusDTO

fun ProductStatus.toDTO() = ProductStatusDTO.valueOf(name)
