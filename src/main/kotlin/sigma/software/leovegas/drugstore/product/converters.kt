package sigma.software.leovegas.drugstore.product

fun Product.convertToProductResponse(): ProductResponse =
    ProductResponse(
        id = id,
        name = name,
        price = price,
        quantity = quantity
    )


fun ProductRequest.convertToProduct(): Product =
    Product(
        id = id,
        name = name,
        price = price,
        quantity = quantity
    )


fun MutableList<Product>.convertToProductResponseList(): MutableList<ProductResponse> {
    return this.map { it.convertToProductResponse() }.toMutableList()
}
