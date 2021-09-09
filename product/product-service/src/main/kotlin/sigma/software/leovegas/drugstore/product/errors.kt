package sigma.software.leovegas.drugstore.product

open class ProductServiceException(message: String) : RuntimeException(message)

class ResourceNotFoundException(productNumber: Long) : ProductServiceException("Product($productNumber) not found")

class NotCorrectRequestException(message: String) : ProductServiceException(message)

class NotEnoughQuantityProductException(message: String) : ProductServiceException(message)

class OrderServerNotAvailableException(message: String) : ProductServiceException(message)