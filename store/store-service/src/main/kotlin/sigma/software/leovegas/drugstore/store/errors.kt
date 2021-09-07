package sigma.software.leovegas.drugstore.store

open class StoreServiceException(message: String) : RuntimeException(message)

class InsufficientAmountOfProductException(productId: Long) : StoreServiceException(
    "Insufficient amount product with id = $productId"
)

class ProductsAlreadyDelivered(orderId: Long) : StoreServiceException("Products from order($orderId) already delivered")

class AccountancyServerResponseException(orderId: Long) :
    StoreServiceException("Can't receive invoice details by order($orderId")

class ProductServerResponseException(orderId: Long) :
    StoreServiceException("Can't reduce product amount by order($orderId)")
