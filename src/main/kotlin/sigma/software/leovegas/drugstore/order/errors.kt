package sigma.software.leovegas.drugstore.order

class OrderNotFoundException(id: Long?) : RuntimeException("Order with $id was not found")

class InsufficientAmountOfOrderItemException :
    RuntimeException("You have to add minimum one order item to create the order")
