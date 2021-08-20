package sigma.software.leovegas.drugstore.product

import javax.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import sigma.software.leovegas.drugstore.order.client.OrderClient
import sigma.software.leovegas.drugstore.product.api.ProductRequest
import sigma.software.leovegas.drugstore.product.api.ProductResponse

@Service
@Transactional
class ProductService(
    private val repo: ProductRepository,
    val orderClient: OrderClient
) {

    companion object {
        private const val exceptionMessage = "This product with id: %d doesn't exist!"
    }

    fun getAll(page: Int, size: Int, search: String, sortField: String, sortDirection: String): Page<ProductResponse> {
        val productsIdsToQuantityMap = orderClient.getProductsIdToQuantity()
        val pageable: Pageable = PageRequest.of(page, size, SortUtil.getSort(sortField, sortDirection))
        val products = repo.findAllById(search, productsIdsToQuantityMap.keys, pageable)
        val totalElements = products.totalElements
        val productResponseList = products.content.toProductResponseList()
        val responseList = productResponseList.map { p -> p.copy(totalBuys = productsIdsToQuantityMap[p.id] ?: 0) }
        if (sortField == "default") {
            val sortedByTotalBuys = responseList.sortedByDescending { e -> e.totalBuys }
            return PageImpl(sortedByTotalBuys, pageable, totalElements)
        }
        return PageImpl(responseList, pageable, totalElements)
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
        repo.save(toUpdate).toProductResponse()
    }

    fun delete(id: Long) {
        getOne(id)
        repo.deleteById(id)
    }
}
