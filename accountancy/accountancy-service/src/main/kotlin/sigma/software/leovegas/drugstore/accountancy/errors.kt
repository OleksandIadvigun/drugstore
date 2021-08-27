package sigma.software.leovegas.drugstore.accountancy

class ResourceNotFoundException(message: String?) : RuntimeException(message)

class PriceItemNotFoundException(id: Long) : RuntimeException("Price Item with id = $id was not found")

class OrderAlreadyHaveInvoice(message: String?) : RuntimeException(message)

class InvalidStatusOfInvoice() : RuntimeException(
    "The invoice status should be CREATED to be paid, but status found is invalid"
)

class NotPaidInvoiceException(id: Long) : RuntimeException("The invoice with id = $id is not paid")
