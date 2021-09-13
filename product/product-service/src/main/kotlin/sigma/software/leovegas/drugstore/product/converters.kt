package sigma.software.leovegas.drugstore.product

import sigma.software.leovegas.drugstore.product.api.CreateProductRequest
import sigma.software.leovegas.drugstore.product.api.CreateProductResponse
import sigma.software.leovegas.drugstore.product.api.DeliverProductsResponse
import sigma.software.leovegas.drugstore.product.api.GetProductResponse
import sigma.software.leovegas.drugstore.product.api.ProductDetailsResponse
import sigma.software.leovegas.drugstore.product.api.ProductStatusDTO
import sigma.software.leovegas.drugstore.product.api.ReceiveProductResponse
import sigma.software.leovegas.drugstore.product.api.SearchProductResponse

// CreateProductRequest <-> Product entity

fun CreateProductRequest.toEntity() = Product(
    name = name,
    quantity = quantity,
    price = price,
)

fun List<CreateProductRequest>.toEntityList() = this.map(CreateProductRequest::toEntity)

fun Product.toCreateProductResponse() =
    CreateProductResponse(
        id = id ?: -1,
        status = status.toDTO(),
        name = name,
        quantity = quantity,
        price = price,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

fun List<Product>.toCreateProductResponseList() = this.map(Product::toCreateProductResponse)

// Product entity -> UpdateProductRequest

fun Product.toReduceProductQuantityResponse() = DeliverProductsResponse(
    id = id ?: -1,
    quantity = quantity,
    updatedAt = updatedAt
)

fun List<Product>.toReduceProductQuantityResponseList() = this.map(Product::toReduceProductQuantityResponse)

// Product entity -> GetProductResponse

fun Product.toGetProductResponse() =
    GetProductResponse(
        productNumber = id ?: -1,
        name = name
    )

fun List<Product>.toGetProductResponseList() = this.map(Product::toGetProductResponse)

// Product entity -> ProductDetailsResponse

fun Product.toProductDetailsResponse() =
    ProductDetailsResponse(
        productNumber = id ?: -1,
        name = name,
        price = price,
        quantity = quantity
    )

fun List<Product>.toProductDetailsResponseList() = this.map(Product::toProductDetailsResponse)

// Product entity -> SearchProductResponse

fun Product.toSearchProductResponse() =
    SearchProductResponse(
        productNumber = id ?: -1,
        name = name,
        quantity = quantity,
        price = price,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

fun List<Product>.toSearchProductResponseList() = this.map(Product::toSearchProductResponse)

// Product entity -> ReduceProductResponse

fun Product.toReceiveProductResponse() =
    ReceiveProductResponse(
        id = id ?: -1,
        status = status.toDTO()
    )

fun List<Product>.toReceiveProductResponseList() = this.map(Product::toReceiveProductResponse)

// ProductStatus -> ProductStatusDTO

fun ProductStatus.toDTO() = ProductStatusDTO.valueOf(name)
