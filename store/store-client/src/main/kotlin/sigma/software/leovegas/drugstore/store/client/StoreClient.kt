package sigma.software.leovegas.drugstore.store.client

import feign.Headers
import feign.Param
import feign.RequestLine
import sigma.software.leovegas.drugstore.store.api.CreateStoreRequest
import sigma.software.leovegas.drugstore.store.api.StoreResponse
import sigma.software.leovegas.drugstore.store.api.UpdateStoreRequest

@Headers("Content-Type: application/json")
interface StoreClient {

    @RequestLine("POST /api/v1/store")
    fun createStoreItem(createStoreRequest: CreateStoreRequest): StoreResponse

    @RequestLine("GET /api/v1/store")
    fun getStoreItems(): List<StoreResponse>

    @RequestLine("GET /api/v1/store/price-ids/?ids={ids}")
    fun getStoreItemsByPriceItemsId(@Param ids: List<Long>): List<StoreResponse>

    @RequestLine("PUT /api/v1/store/increase")
    fun increaseQuantity(requests: List<UpdateStoreRequest>): List<StoreResponse>

    @RequestLine("PUT /api/v1/store/reduce")
    fun reduceQuantity(requests: List<UpdateStoreRequest>): List<StoreResponse>

    @RequestLine("PUT /api/v1/store/check")
    fun checkAvailability(requests: List<UpdateStoreRequest>): List<StoreResponse>

    @RequestLine("PUT /api/v1/store/delivery/{id}")
    fun deliverGoods(@Param id: Long): String
}
