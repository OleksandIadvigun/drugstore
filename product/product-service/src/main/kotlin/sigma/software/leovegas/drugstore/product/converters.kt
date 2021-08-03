package sigma.software.leovegas.drugstore.product

fun Product.convertToProductResponse(): ProductResponse =
    ProductResponse(
        id = id,
        name = name,
        price = price
    )

fun ProductRequest.convertToProduct(): Product =
    Product(
        name = name,
        price = price
    )

fun MutableList<Product>.convertToProductResponseList(): MutableList<ProductResponse> {
    return this.map { it.convertToProductResponse() }.toMutableList()
}
