package sigma.software.leovegas.drugstore.store

open class StoreServiceException(message: String) : RuntimeException(message)

class InsufficientAmountOfProductException(productNumber: String, available: Int) : StoreServiceException(
    "Insufficient amount product with id = $productNumber. Available only $available items."
)

class ProductsAlreadyDelivered(orderNumber: String) :
    StoreServiceException("Products from order($orderNumber) already delivered.")

class AccountancyServerResponseException(message: String) :
    StoreServiceException("Ups... some problems in accountancy service. $message.")

class ProductServerResponseException(message: String) :
    StoreServiceException("Ups... some problems in product service. $message.")

class NotCorrectQuantityException() :
    StoreServiceException("Quantity in request should be grater than 0.")

class NotCorrectRequestException() :
    StoreServiceException("Request body is not valid. Please, fulfill all necessary fields.")
