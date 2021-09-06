package sigma.software.leovegas.drugstore.accountancy

class ResourceNotFoundException(message: String?) : RuntimeException(message)

class OrderAlreadyHaveInvoice(message: String?) : RuntimeException(message)

class InvalidStatusOfInvoice() : RuntimeException(
    "The invoice status should be CREATED to be paid, but status found is invalid"
)

class NotPaidInvoiceException(id: Long) : RuntimeException("The invoice with id = $id is not paid")

class ProductServiceResponseException() : RuntimeException("Ups... Something went wrong! Please, try again later")

class OrderServiceResponseException() : RuntimeException("Ups... Something went wrong! Please, try again later")

class StoreServiceResponseException() : RuntimeException("Ups... Something went wrong! Please, try again later")

class NotEnoughMoneyException() : RuntimeException("Not enough money for this transaction!")
