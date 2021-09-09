package sigma.software.leovegas.drugstore.order

open class OrderServiceException(message: String) : RuntimeException(message)

class OrderNotFoundException(id: Long?) : OrderServiceException("Order with $id was not found")

class InsufficientAmountOfOrderItemException :
    OrderServiceException("You have to add minimum one order item")

class ProductServerNotAvailableException : OrderServiceException("We can't get products. Try again later")

class AccountancyServerNotAvailableException : OrderServiceException("We can't create invoice. Try again later")

class OrderNotCreatedException(id: Long) : OrderServiceException("Order must be created")

class OrderStatusException(message: String) : OrderServiceException(message)

class OrderRequestException(message: String) : OrderServiceException(message)
