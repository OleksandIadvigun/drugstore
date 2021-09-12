package sigma.software.leovegas.drugstore.product

open class ProductServiceException(message: String) : RuntimeException(message)

class ResourceNotFoundException(productNumber: Long) :
    ProductServiceException("Product($productNumber) not found.")

class NotCorrectRequestException(message: String) : ProductServiceException(message)

class NotEnoughQuantityProductException(message: String) : ProductServiceException(message)

class OrderServerException(message: String) :
    ProductServiceException("Ups... some problems in order service. $message.")

class AccountancyServerException(message: String) :
    ProductServiceException("Ups... some problems in accountancy service. $message.")