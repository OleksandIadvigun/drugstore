package sigma.software.leovegas.drugstore.order

class OrderNotFoundException(id: Long?) : RuntimeException("Order with $id was not found")

class InsufficientAmountOfOrderItemException :
    RuntimeException("You have to add minimum one order item")

class ProductServerNotAvailable : RuntimeException("We can't get products. Try again later")

class AccountancyServerNotAvailable : RuntimeException("We can't create invoice. Try again later")

class OrderNotCreatedException(id: Long) : RuntimeException("Order must be created")

class OrderStatusException(message: String) : RuntimeException(message)
