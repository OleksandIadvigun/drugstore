package sigma.software.leovegas.drugstore.stock

data class StockRequest(
    val productId: Long? = null,
    val quantity: Int
)

data class StockResponse(
    val id: Long? = null,
    val productId: Long? = null,
    val quantity: Int
)
