package sigma.software.leovegas.drugstore.store

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import sigma.software.leovegas.drugstore.store.api.CreateStoreRequest
import sigma.software.leovegas.drugstore.store.api.UpdateStoreRequest

@Service
@Transactional
class StoreService(private val storeRepository: StoreRepository) {

    fun create(storeRequest: CreateStoreRequest) = storeRequest.run {
        if (getStoreItemsByPriceItemIds(listOf(this.priceItemId)).isNotEmpty()) {
            throw StoreItemWithThisPriceItemAlreadyExistException()
        }
        storeRepository.save(toEntity()).toStoreResponseDTO()
    }

    fun getStoreItemsByPriceItemIds(ids: List<Long>) = storeRepository.getStoreByPriceItemIds(ids)


    fun getStoreItems() = storeRepository.findAll().toStoreResponseList()

    fun increaseQuantity(request: List<UpdateStoreRequest>) = request.run {
        val map = this.associate { it.priceItemId to it.quantity }
        val priceItemIds = this.map { it.priceItemId }
        val storeItems = getStoreItemsByPriceItemIds(priceItemIds)
        storeItems.map { it.copy(quantity = map[it.priceItemId]?.plus(it.quantity) ?: -1).toStoreResponseDTO() }
    }

    fun reduceQuantity(request: List<UpdateStoreRequest>) = request.run {
        checkAvailability(request)
        val map = this.associate { it.priceItemId to it.quantity }
        val priceItemIds = this.map { it.priceItemId }
        val storeItems = getStoreItemsByPriceItemIds(priceItemIds)
        storeItems.map { it.copy(quantity = it.quantity.minus(map[it.priceItemId] ?: -1)).toStoreResponseDTO() }
    }

    fun checkAvailability(request: List<UpdateStoreRequest>) = request.run {
        val priceItemIds = this.map { it.priceItemId }
        val storeItems = getStoreItemsByPriceItemIds(priceItemIds)
        val map = storeItems.associate { it.priceItemId to it.quantity }
        forEach {
            if (it.quantity > (map[it.priceItemId] ?: -1)) {
                throw InsufficientAmountOfStoreItemException(it.priceItemId)
            }
        }
        true
    }

    fun deliverGoods(orderId: Long) {
        // TODO: 8/18/2021  check invoice with orderId is PAID ->
        // TODO: 8/18/2021  change order status DELIVERED
    }
}
