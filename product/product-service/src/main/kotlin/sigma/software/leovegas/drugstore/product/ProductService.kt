package sigma.software.leovegas.drugstore.product

import java.math.BigDecimal
import javax.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import sigma.software.leovegas.drugstore.order.client.OrderClient
import sigma.software.leovegas.drugstore.product.api.CreateProductRequest
import sigma.software.leovegas.drugstore.product.api.DeliverProductsQuantityRequest
import sigma.software.leovegas.drugstore.product.api.GetProductResponse
import sigma.software.leovegas.drugstore.product.api.ReturnProductQuantityRequest
import sigma.software.leovegas.drugstore.product.api.SearchProductResponse

@Service
@Transactional
class ProductService(
    private val productRepository: ProductRepository,
    val orderClient: OrderClient,
) {

    companion object {
        private const val exceptionMessage = "This product with id: %d doesn't exist!"
    }

    fun searchProducts(page: Int, size: Int, search: String, sortField: String, sortDirection: String)
            : Page<SearchProductResponse> {
        if (search == "") throw NotCorrectRequestException("Search field can not be empty!")
        if (sortField == "popularity") {
            val pageableForPopularity: Pageable = PageRequest.of(page, size)
            val productsQuantity = runCatching { orderClient.getProductsIdToQuantity() }
                .onFailure { throw OrderServerNotAvailableException("Something's wrong, please try again later") }
                .getOrThrow()

            val products = productRepository
                .findAllByNameContainingAndIdInAndStatusAndQuantityGreaterThan(
                    search,
                    productsQuantity.keys,
                    ProductStatus.RECEIVED,
                    0,
                    pageableForPopularity
                )
                .map(Product::toSearchProductResponse)
            val index = productsQuantity.keys.withIndex().associate { it.value to it.index }
            val sortedContent = products.sortedBy { index[it.id] }
            return PageImpl(sortedContent, pageableForPopularity, products.totalElements)

        }
        val pageable: Pageable = PageRequest.of(page, size, SortUtil.getSort(sortField, sortDirection))
        val products = productRepository.findAllByNameContainingAndStatusAndQuantityGreaterThan(
            search, ProductStatus.RECEIVED, 0, pageable
        )
        return products.map(Product::toSearchProductResponse)
    }

    fun getPopularProducts(page: Int, size: Int):
            Page<GetProductResponse> {
        val pageableForPopularity: Pageable = PageRequest.of(page, size)
        val productsIdToQuantity: Map<Long, Int>
        try {
            productsIdToQuantity = orderClient.getProductsIdToQuantity()
        } catch (e: Exception) {
            throw OrderServerNotAvailableException("Something's wrong, please try again later")
        }
        val products = productRepository
            .findAllByIdInAndStatusAndQuantityGreaterThan(
                productsIdToQuantity.keys, ProductStatus.RECEIVED, 0, pageableForPopularity
            )
            .map(Product::toGetProductResponse)
        val index = productsIdToQuantity.keys.withIndex().associate { it.value to it.index }
        val sortedContent = products.sortedBy { index[it.id] }
        return PageImpl(sortedContent, pageableForPopularity, products.totalElements)
    }

    fun getProductsDetailsByIds(ids: List<Long>) = productRepository.findAllById(ids).toProductDetailsResponseList()

    fun createProduct(productRequest: List<CreateProductRequest>) = productRequest.validate().run {
        productRepository.saveAll(toEntityList()).toCreateProductResponseList()
    }

    fun receiveProducts(ids: List<Long>) =
        productRepository
            .findAllById(ids)
            .map { it.copy(status = ProductStatus.RECEIVED) }
            .toReceiveProductResponseList()

    fun deliverProducts(productRequest: List<DeliverProductsQuantityRequest>) = productRequest.validate().run {
        val idsToQuantity = associate { it.id to it.quantity }
        val toUpdate = productRepository
            .findAllById(this.map { it.id })
            .map {
                if (it.quantity < (idsToQuantity[it.id] ?: -1)) {
                    throw NotEnoughQuantityProductException("Not enough available quantity of product with id: ${it.id}")
                }
                it.copy(quantity = it.quantity.minus(idsToQuantity[it.id] ?: -1))
            }
        productRepository.saveAllAndFlush(toUpdate).toReduceProductQuantityResponseList()
    }

    fun returnProducts(products: List<ReturnProductQuantityRequest>) = products.validate().run {
        val idsToQuantity = associate { it.id to it.quantity }
        val toUpdate = productRepository
            .findAllById(this.map { it.id })
            .map { it.copy(quantity = it.quantity.plus(idsToQuantity[it.id] ?: -1)) }
        productRepository.saveAllAndFlush(toUpdate).toReduceProductQuantityResponseList()
    }

    fun getProductPrice(productNumber: Long): BigDecimal =
        productNumber.validate(productRepository::findFirstByIdOrderByCreatedAtDesc).price
}
