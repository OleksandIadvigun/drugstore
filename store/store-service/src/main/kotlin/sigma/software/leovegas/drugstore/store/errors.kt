package sigma.software.leovegas.drugstore.store

class InsufficientAmountOfProductException(id: Long) : RuntimeException(
    "Insufficient amount of store with price item id = $id "
)

class InvoiceNotPaidException(id: Long) : RuntimeException(
    "Invoice with id = $id not paid !"
)

class IncorrectTypeOfInvoice(message: String) : RuntimeException(message)

class IncorrectStatusOfInvoice(message: String) : RuntimeException(message)
