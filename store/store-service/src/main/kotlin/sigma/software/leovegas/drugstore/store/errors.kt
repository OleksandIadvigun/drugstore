package sigma.software.leovegas.drugstore.store

class StoreItemWithThisPriceItemAlreadyExistException() : RuntimeException(
    "Store with this price item already exist!"
)

class InsufficientAmountOfStoreItemException(id: Long) : RuntimeException(
    "Insufficient amount of store with price item id = $id "
)

