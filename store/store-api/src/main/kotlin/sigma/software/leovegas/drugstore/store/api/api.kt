package sigma.software.leovegas.drugstore.store.api

// Requests

data class CreateStoreRequest(
    val priceItemId: Long = -1,
    val quantity: Int = -1
)

data class UpdateStoreRequest(
    val priceItemId: Long = -1,
    val quantity: Int = -1
)

// Responses

data class StoreResponse(
    val id: Long = -1,
    val priceItemId: Long = -1,
    val quantity: Int = -1,
)
