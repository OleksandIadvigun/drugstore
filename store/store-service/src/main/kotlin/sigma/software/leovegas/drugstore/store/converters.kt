package sigma.software.leovegas.drugstore.store

import sigma.software.leovegas.drugstore.store.api.CreateStoreRequest
import sigma.software.leovegas.drugstore.store.api.StoreResponse

// StoreRequest -> Store entity

fun CreateStoreRequest.toEntity() = Store(
    priceItemId = priceItemId,
    quantity = quantity
)

// Store entity -> StoreResponse

fun Store.toStoreResponseDTO() = StoreResponse(
    id = id ?: -1,
    priceItemId = priceItemId,
    quantity = quantity
)

fun List<Store>.toStoreResponseList() = this.map(Store::toStoreResponseDTO)
