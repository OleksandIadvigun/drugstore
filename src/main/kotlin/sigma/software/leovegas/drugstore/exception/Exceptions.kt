package sigma.software.leovegas.drugstore.exception

class OrderNotFoundException(id:Long): Exception(
    "Order with $id was not found")

class InsufficientAmountOfProductForOrderException(): Exception(
    "You have to add minimum one product to create the order")



class ResourceNotFoundException(message: String?) : RuntimeException(message)
