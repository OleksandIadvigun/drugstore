package sigma.software.leovegas.drugstore.store

class StoreItemWithThisPriceItemAlreadyExistException(id: Long) : RuntimeException(
    "Store with this price item id = $id already exist!"
)

class InsufficientAmountOfStoreItemException(id: Long) : RuntimeException(
    "Insufficient amount of store with price item id = $id "
)

class InvoiceNotPaidException(id: Long) : RuntimeException(
    "Invoice with id = $id not paid !"
)

