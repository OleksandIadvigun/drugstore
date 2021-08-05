package sigma.software.leovegas.drugstore.stock

class StockNotFoundException(id: Long?) : RuntimeException(
    "Stock with id $id is not found!"
)

class StockWithThisProductAlreadyExistException() : RuntimeException(
    "Stock with this product already exist!"
)

class ProductIsNotExistException(id: Long?) : RuntimeException(
    "Product with id: $id is not exist!"
)
