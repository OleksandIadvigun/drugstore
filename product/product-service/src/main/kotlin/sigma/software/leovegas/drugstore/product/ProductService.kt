package sigma.software.leovegas.drugstore.product

import java.math.BigDecimal
import javax.transaction.Transactional
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import sigma.software.leovegas.drugstore.accountancy.client.AccountancyClient
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
    val accountancyClient: AccountancyClient
) {

    val logger: Logger = LoggerFactory.getLogger(ProductService::class.java)

    fun searchProducts(page: Int, size: Int, search: String, sortField: String, sortDirection: String)
            : List<SearchProductResponse> {
        if (search == "") throw NotCorrectRequestException("Search field can not be empty!")

        if (sortField == "popularity") {
            val pageableForPopularity: Pageable = PageRequest.of(page, size)
            val productsQuantity = runCatching { orderClient.getProductsIdToQuantity() }
                .onFailure { throw OrderServerNotAvailableException("Something's wrong, please try again later") }
                .getOrThrow()
            logger.info("Received products quantity $productsQuantity")

            val products = productRepository
                .findAllByNameContainingAndIdInAndStatusAndQuantityGreaterThan(
                    search,
                    productsQuantity.keys,
                    ProductStatus.RECEIVED,
                    0,
                    pageableForPopularity
                )
            logger.info("Received popular products $products")

            val productsPrice = accountancyClient.getSalePrice(products.map { it.id ?: -1 })
            logger.info("Received products price $productsPrice")

            val productForSale = products.map { it.copy(price = productsPrice[it.id] ?: BigDecimal.ZERO) }
            val index = productsQuantity.keys.withIndex().associate { it.value to it.index }
            return productForSale.sortedBy { index[it.id] }.toSearchProductResponseList()
        }
        val pageable: Pageable = PageRequest.of(page, size, SortUtil.getSort(sortField, sortDirection))
        val products = productRepository.findAllByNameContainingAndStatusAndQuantityGreaterThan(
            search, ProductStatus.RECEIVED, 0, pageable
        )
        logger.info("Received sorted by $sortField products $products")

        val productsPrice = accountancyClient.getSalePrice(products.map { it.id ?: -1 })
        logger.info("Received products price $productsPrice")

        val productForSale = products.map { it.copy(price = productsPrice[it.id] ?: BigDecimal.ZERO) }
        return productForSale.toSearchProductResponseList()
    }

    fun getPopularProducts(page: Int, size: Int):
            List<GetProductResponse> {
        val pageableForPopularity: Pageable = PageRequest.of(page, size)
        val productsQuantity = runCatching { orderClient.getProductsIdToQuantity() }
            .onFailure { throw OrderServerNotAvailableException("Something's wrong, please try again later") }
            .getOrThrow()
        logger.info("Received products quantity $productsQuantity")

        val products = productRepository
            .findAllByIdInAndStatusAndQuantityGreaterThan(
                productsQuantity.keys, ProductStatus.RECEIVED, 0, pageableForPopularity
            )
            .map(Product::toGetProductResponse)
        logger.info("Received popular products $products")
        val index = productsQuantity.keys.withIndex().associate { it.value to it.index }
        return products.sortedBy { index[it.id] }
    }

    fun getProductsDetailsByIds(ids: List<Long>) =
        ids.run {
            val products = productRepository.findAllById(this)
            logger.info("Products $products")
            products.toProductDetailsResponseList()
        }

    fun createProduct(productRequest: List<CreateProductRequest>) =
        productRequest.validate().run {
            val savedProducts = productRepository.saveAll(toEntityList())
            logger.info("Saved Products $savedProducts")
            savedProducts.toCreateProductResponseList()
        }

    fun receiveProducts(ids: List<Long>) = ids.run {
        val productsReceived = productRepository.findAllById(ids).map { it.copy(status = ProductStatus.RECEIVED) }
        logger.info("Received Products $productsReceived")
        productsReceived.toReceiveProductResponseList()
    }

    fun deliverProducts(productRequest: List<DeliverProductsQuantityRequest>) =
        productRequest.validate().run {
            val idsToQuantity = associate { it.id to it.quantity }
            val toUpdate = productRepository
                .findAllById(this.map { it.id })
                .map {
                    if (it.quantity < (idsToQuantity[it.id] ?: -1)) {
                        throw NotEnoughQuantityProductException("Not enough available quantity of product with id: ${it.id}")
                    }
                    it.copy(quantity = it.quantity.minus(idsToQuantity[it.id] ?: -1))
                }

            val productsDelivered =
                productRepository.saveAllAndFlush(toUpdate)
            logger.info("Delivered Products $productsDelivered")
            return@run productsDelivered.toReduceProductQuantityResponseList()
        }

    fun returnProducts(products: List<ReturnProductQuantityRequest>) =
        products.validate().run {
            val idsToQuantity = associate { it.id to it.quantity }
            val toUpdate = productRepository
                .findAllById(this.map { it.id })
                .map { it.copy(quantity = it.quantity.plus(idsToQuantity[it.id] ?: -1)) }
            val productsReturned = productRepository.saveAllAndFlush(toUpdate)
            logger.info("Returned Products $productsReturned")
            return@run productsReturned.toReduceProductQuantityResponseList()
        }

    fun getProductPrice(productNumbers: List<Long>): Map<Long, BigDecimal> =
        productNumbers.validate().run {
            val productsPrice = productRepository.findAllByIdInOrderByCreatedAtDesc(productNumbers)
                .associate { (it.id to it.price) as Pair<Long, BigDecimal> }
            logger.info("Products price $productsPrice")
            return@run productsPrice
        }
}

