package sigma.software.leovegas.drugstore.store

open class StoreServiceException(message: String) : RuntimeException(message)

class InsufficientAmountOfProductException(productId: Long) : StoreServiceException(
    "Insufficient amount product with id = $productId"
)

class ProductsAlreadyDelivered(orderId: Long) : StoreServiceException("Products from order($orderId) already delivered")

class AccountancyServerResponseException(orderId: Long) :
    StoreServiceException("Can't receive invoice details by order($orderId)")

class ProductServerResponseException() :
    StoreServiceException("Ups... some problems with product service")

class NotCorrectQuantityException() :
    StoreServiceException("Quantity in request should be grater than 0")

class NotCorrectRequestException() :
    StoreServiceException("Request body is not valid. Please, fulfill all necessary fields")
