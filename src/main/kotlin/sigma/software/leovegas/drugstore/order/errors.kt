package sigma.software.leovegas.drugstore.order

class OrderNotFoundException(id: Long?) : RuntimeException("Order with $id was not found")

class InsufficientAmountOfProductForOrderException :
    RuntimeException("You have to add minimum one product to create the order")
