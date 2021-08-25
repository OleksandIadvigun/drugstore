package sigma.software.leovegas.drugstore.accountancy

class ResourceNotFoundException(message: String?) : RuntimeException(message)

class PriceItemNotFoundException(id: Long) : RuntimeException("Price Item with id = $id was not found")

class OrderAlreadyHaveInvoice(message: String?) : RuntimeException(message)
