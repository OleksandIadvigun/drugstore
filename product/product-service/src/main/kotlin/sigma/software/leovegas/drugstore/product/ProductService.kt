package sigma.software.leovegas.drugstore.product

import javax.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import sigma.software.leovegas.drugstore.accountancy.api.PriceItemResponse
import sigma.software.leovegas.drugstore.accountancy.client.AccountancyClient
import sigma.software.leovegas.drugstore.order.client.OrderClient
import sigma.software.leovegas.drugstore.product.api.ProductRequest
import sigma.software.leovegas.drugstore.product.api.ProductResponse

@Service
@Transactional
class ProductService(
    private val repo: ProductRepository,
    val orderClient: OrderClient,
    val accountancyClient: AccountancyClient
) {

    companion object {
        private const val exceptionMessage = "This product with id: %d doesn't exist!"
    }

    fun getAll(page: Int, size: Int, search: String, sortField: String, sortDirection: String): Page<ProductResponse> {
        val pageable: Pageable = PageRequest.of(page, size, SortUtil.getSort(sortField, sortDirection))
        if (sortField == "default") {
            val productsIdsToQuantityMap = orderClient.getProductsIdToQuantity()
            val products = repo.findAllById(search, productsIdsToQuantityMap.keys, pageable)
            val totalElements = products.totalElements
            val productResponseList = products.content.toProductResponseList()
            val responseList =
                productResponseList.map { p -> p.copy(totalBuys = productsIdsToQuantityMap[p.id] ?: 0) }
            val sortedByTotalBuys =
                if (sortDirection == "DESC") {
                    responseList.sortedByDescending { e -> e.totalBuys }
                } else responseList.sortedBy { e -> e.totalBuys }
            return PageImpl(sortedByTotalBuys, pageable, totalElements)
        } else if (sortField == "name" || sortField == "price" || sortField == "createdAt") {
            val responseList = repo.findAll(search, pageable)
            val prices = accountancyClient.getProductsPriceByProductIds(responseList.content.map { el -> el.id ?: -1 })
            val totalElements = responseList.totalElements
            val convertedToDto = responseList.content.toProductResponseList().associateBy { it.id }
            var response = listOf<ProductResponse?>()
            val productsWithPrice: List<ProductResponse?> = prices.map { el -> convertedToDto[el.productId]?.copy(price = el.price) }
            when (sortField) {
                "name" -> {
                    response = if (sortDirection == "DESC") {
                        productsWithPrice.sortedByDescending { it?.name }
                    } else productsWithPrice.sortedBy { it?.name }
                }
                "price" -> {
                    response = if (sortDirection == "DESC") {
                        productsWithPrice.sortedByDescending { it?.price }
                    } else productsWithPrice.sortedBy { it?.price }
                }
                "createdAt" -> {
                    response = if (sortDirection == "DESC") {
                        productsWithPrice.sortedByDescending { it?.createdAt }
                    } else productsWithPrice.sortedBy { it?.createdAt }
                }
            }

            return PageImpl(response, pageable, totalElements)
        }
        throw ResourceNotFoundException("Incorrect sort field name")
    }

    fun getProductsByIds(ids: List<Long>): List<ProductResponse> = repo.findAllById(ids).toProductResponseList()

    fun create(productRequest: ProductRequest): ProductResponse = productRequest.run {
        repo.save(toEntity()).toProductResponse()
    }

    fun getOne(id: Long): ProductResponse =
        repo.findById(id).orElseThrow { throw ResourceNotFoundException(String.format(exceptionMessage, id)) }
            .toProductResponse()

    fun update(id: Long, productRequest: ProductRequest): ProductResponse = productRequest.run {
        val toUpdate = repo
            .findById(id)
            .orElseThrow { throw ResourceNotFoundException(String.format(exceptionMessage, id)) }
            .copy(name = name)
        repo.saveAndFlush(toUpdate).toProductResponse()
    }

    fun delete(id: Long) {
        getOne(id)
        repo.deleteById(id)
    }
}
