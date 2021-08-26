package sigma.software.leovegas.drugstore.store

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import sigma.software.leovegas.drugstore.accountancy.api.InvoiceStatusDTO
import sigma.software.leovegas.drugstore.accountancy.client.AccountancyClient
import sigma.software.leovegas.drugstore.order.api.OrderStatusDTO
import sigma.software.leovegas.drugstore.order.client.OrderClient
import sigma.software.leovegas.drugstore.store.api.CreateStoreRequest
import sigma.software.leovegas.drugstore.store.api.UpdateStoreRequest

@Service
@Transactional
class StoreService @Autowired constructor(
    val storeRepository: StoreRepository,
    val orderClient: OrderClient,
    val accountancyClient: AccountancyClient
) {

    fun createStoreItem(storeRequest: CreateStoreRequest) = storeRequest.run {
        if (getStoreItemsByPriceItemsId(listOf(this.priceItemId)).isNotEmpty()) {
            throw StoreItemWithThisPriceItemAlreadyExistException(this.priceItemId)
        }
        storeRepository.save(toEntity()).toStoreResponseDTO()
    }

    fun getStoreItemsByPriceItemsId(ids: List<Long>) = storeRepository.getStoreByPriceItemIds(ids).toStoreResponseList()


    fun getStoreItems() = storeRepository.findAll().toStoreResponseList()

    fun increaseQuantity(request: List<UpdateStoreRequest>) = request.run {
        val map = this.associate { it.priceItemId to it.quantity }
        val priceItemIds = this.map { it.priceItemId }
        val storeItems = getStoreItemsByPriceItemsId(priceItemIds)
        storeItems.map { it.copy(quantity = map[it.priceItemId]?.plus(it.quantity) ?: -1) }
    }

    fun reduceQuantity(request: List<UpdateStoreRequest>) = request.run {
        checkAvailability(request)
        val map = this.associate { it.priceItemId to it.quantity }
        val priceItemIds = this.map { it.priceItemId }
        val storeItems = getStoreItemsByPriceItemsId(priceItemIds)
        storeItems.map { it.copy(quantity = it.quantity.minus(map[it.priceItemId] ?: -1)) }
    }

    fun checkAvailability(request: List<UpdateStoreRequest>) = request.run {
        val priceItemIds = this.map { it.priceItemId }
        val storeItems = getStoreItemsByPriceItemsId(priceItemIds)
        val map = storeItems.associate { it.priceItemId to it.quantity }
        forEach {
            if (it.quantity > (map[it.priceItemId] ?: -1)) {
                throw InsufficientAmountOfStoreItemException(it.priceItemId)
            }
        }
        storeItems
    }

    fun deliverGoods(orderId: Long) :String {
        val invoice = accountancyClient.getInvoiceByOrderId(orderId)
        if (invoice.status != InvoiceStatusDTO.PAID) {
            throw InvoiceNotPaidException(invoice.id)
        }
        orderClient.changeOrderStatus(invoice.orderId, OrderStatusDTO.DELIVERED)
        return "DELIVERED"
    }
}
