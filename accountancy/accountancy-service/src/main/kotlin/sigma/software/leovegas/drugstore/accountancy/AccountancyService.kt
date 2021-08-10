package sigma.software.leovegas.drugstore.accountancy

import javax.transaction.Transactional
import org.springframework.stereotype.Service
import sigma.software.leovegas.drugstore.accountancy.api.PriceItemRequest
import sigma.software.leovegas.drugstore.accountancy.api.PriceItemResponse

@Service
@Transactional
class AccountancyService(private val repo: PriceItemRepository) {

    companion object {
        private const val exceptionMessage = "This price item with id: %d doesn't exist!"
    }

    fun createPriceItem(priceItemRequest: PriceItemRequest): PriceItemResponse = priceItemRequest.run {
        repo.save(toEntity()).toPriceItemResponse()
    }

    fun updatePriceItem(id: Long, priceItemRequest: PriceItemRequest): PriceItemResponse = priceItemRequest.run {
        val toUpdate = repo
            .findById(id)
            .orElseThrow { throw ResourceNotFoundException(String.format(exceptionMessage, id)) }
            .copy(productId = productId, price = price)
        repo.save(toUpdate).toPriceItemResponse()
    }
}
