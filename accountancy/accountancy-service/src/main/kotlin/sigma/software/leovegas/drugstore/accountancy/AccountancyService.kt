package sigma.software.leovegas.drugstore.accountancy

import java.math.BigDecimal
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
        repo.saveAndFlush(toUpdate).toPriceItemResponse()
    }

    fun getProductsPrice(): Map<Long?, BigDecimal> = repo.findAll().associate { it.productId to it.price }

    fun getProductsPriceByIds(ids: List<Long>): Map<Long?, BigDecimal> =
        repo.findAllByProductId(ids).associate { it.productId to it.price }
}
