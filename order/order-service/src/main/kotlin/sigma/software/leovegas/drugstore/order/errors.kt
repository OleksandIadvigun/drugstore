package sigma.software.leovegas.drugstore.order

open class OrderServiceException(message: String) : RuntimeException(message)

class OrderNotFoundException(orderNumber: String) : OrderServiceException("Order with $orderNumber was not found.")

class InsufficientAmountOfOrderItemException :
    OrderServiceException("You have to add minimum one order item.")

class ProductServerException(message: String) :
    OrderServiceException("Ups... some problems in product service. $message.")

class AccountancyServerException(message: String) :
    OrderServiceException("Ups... some problems in accountancy service. $message.")

class OrderNotCreatedException(orderNumber: String) : OrderServiceException("Order($orderNumber) must be created.")

class OrderStatusException(message: String) : OrderServiceException(message)

class OrderRequestException(message: String) : OrderServiceException(message)

class RabbitServerNotAvailable(message: String) :
    OrderServiceException("Ups... some problems in rabbit service. $message.")
