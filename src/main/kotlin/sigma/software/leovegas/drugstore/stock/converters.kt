package sigma.software.leovegas.drugstore.stock

fun StockRequest.convertToStock() = Stock(
    productId = productId,
    quantity = quantity
)

fun Stock.convertToStockResponse() = StockResponse(
    id = id,
    productId = productId,
    quantity = quantity
)

fun MutableList<Stock>.convertToListOfStockResponses() = this.map { it.convertToStockResponse() }.toMutableList()
