package sigma.software.leovegas.drugstore.product

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
