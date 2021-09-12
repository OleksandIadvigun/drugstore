package sigma.software.leovegas.drugstore.order

open class OrderServiceException(message: String) : RuntimeException(message)

class OrderNotFoundException(id: Long?) : OrderServiceException("Order with $id was not found.")

class InsufficientAmountOfOrderItemException :
    OrderServiceException("You have to add minimum one order item.")

class ProductServerException(message: String) :
    OrderServiceException("Ups... some problems in product service. $message.")

class AccountancyServerException(message: String) :
    OrderServiceException("Ups... some problems in accountancy service. $message.")

class OrderNotCreatedException(id: Long) : OrderServiceException("Order($id) must be created.")

class OrderStatusException(message: String) : OrderServiceException(message)

class OrderRequestException(message: String) : OrderServiceException(message)
