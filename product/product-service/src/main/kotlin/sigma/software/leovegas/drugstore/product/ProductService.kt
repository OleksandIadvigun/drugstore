package sigma.software.leovegas.drugstore.product

import java.math.BigDecimal
import javax.transaction.Transactional
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import sigma.software.leovegas.drugstore.accountancy.client.AccountancyClient
import sigma.software.leovegas.drugstore.accountancy.client.proto.AccountancyClientProto
import sigma.software.leovegas.drugstore.api.messageSpliterator
import sigma.software.leovegas.drugstore.api.protobuf.Proto
import sigma.software.leovegas.drugstore.api.toBigDecimal
import sigma.software.leovegas.drugstore.api.toDecimalProto
import sigma.software.leovegas.drugstore.order.client.OrderClient
import sigma.software.leovegas.drugstore.product.api.CreateProductsEvent
import sigma.software.leovegas.drugstore.product.api.GetProductResponse
import sigma.software.leovegas.drugstore.product.api.SearchProductResponse

@Service
@Transactional
class ProductService(
    private val productRepository: ProductRepository,
    val orderClient: OrderClient,
    val accountancyClient: AccountancyClient,
    val accountancyClientProto: AccountancyClientProto
) {

    val logger: Logger = LoggerFactory.getLogger(ProductService::class.java)

    fun searchProducts(page: Int, size: Int, search: String, sortField: String, sortDirection: String)
            : List<SearchProductResponse> {
        if (search == "") throw NotCorrectRequestException("Search field can not be empty!")

        if (sortField == "popularity") {
            val pageableForPopularity: Pageable = PageRequest.of(page, size)
            val productsQuantity = runCatching { orderClient.getProductsIdToQuantity() }
                .onFailure { error -> throw OrderServerException(error.localizedMessage.messageSpliterator()) }
                .getOrThrow()
            logger.info("Received products quantity $productsQuantity")

            val products = productRepository
                .findAllByNameContainingAndProductNumberInAndStatusAndQuantityGreaterThan(
                    search,
                    productsQuantity.keys,
                    ProductStatus.RECEIVED,
                    0,
                    pageableForPopularity
                )
            logger.info("Received popular products $products")
            if (products.isEmpty()) return listOf()

            val productsPrice = runCatching {
                accountancyClientProto.getSalePrice(products.map { it.productNumber })
            }
                .onFailure { error -> throw AccountancyServerException(error.localizedMessage.messageSpliterator()) }
                .getOrThrow()
            logger.info("Received products price $productsPrice")

            val productForSale = products.map {
                it.copy(
                    price = productsPrice.itemsMap[it.productNumber]?.toBigDecimal() ?: BigDecimal.ZERO
                )
            }  //todo
            val index = productsQuantity.keys.withIndex().associate { it.value to it.index }
            return productForSale.sortedBy { index[it.productNumber] }.toSearchProductResponseList()
        }
        val pageable: Pageable = PageRequest.of(page, size, SortUtil.getSort(sortField, sortDirection))
        val products = productRepository.findAllByNameContainingAndStatusAndQuantityGreaterThan(
            search, ProductStatus.RECEIVED, 0, pageable
        )
        logger.info("Received sorted by $sortField products $products")
        if (products.isEmpty()) return listOf()

        val productsPrice = runCatching {
            accountancyClientProto.getSalePrice(products.map { it.productNumber })
        }
            .onFailure { error -> throw AccountancyServerException(error.localizedMessage.messageSpliterator()) }
            .getOrThrow()
        logger.info("Received products price $productsPrice")

        val productForSale = products.map {
            it.copy(
                price = productsPrice.itemsMap[it.productNumber]?.toBigDecimal() ?: BigDecimal.ZERO
            )
        }
        return productForSale.toSearchProductResponseList()
    }

    fun getPopularProducts(page: Int, size: Int): List<GetProductResponse> {
        val pageableForPopularity: Pageable = PageRequest.of(page, size)
        val productsQuantity = runCatching { orderClient.getProductsIdToQuantity() }
            .onFailure { error -> throw OrderServerException(error.localizedMessage.messageSpliterator()) }
            .getOrThrow()
        logger.info("Received products quantity $productsQuantity")

        val products = productRepository
            .findAllByProductNumberInAndStatusAndQuantityGreaterThan(
                productsQuantity.keys, ProductStatus.RECEIVED, 0, pageableForPopularity
            )
            .map(Product::toGetProductResponse)
        logger.info("Received popular products $products")
        val index = productsQuantity.keys.withIndex().associate { it.value to it.index }
        return products.sortedBy { index[it.productNumber] }
    }

    fun getProductsDetailsByProductNumbers(productNumbers: List<String>) =
        productNumbers.run {
            val products = productRepository.findAllByProductNumberInAndStatus(this, ProductStatus.RECEIVED)
            logger.info("Products $products")
            val productProto = products.map {
                Proto.ProductDetailsItem.newBuilder()
                    .setProductNumber(it.productNumber)
                    .setQuantity(it.quantity)
                    .setPrice(it.price.toDecimalProto())
                    .setName(it.name)
                    .build()
            }
            return@run Proto.ProductDetailsResponse.newBuilder().addAllProducts(productProto).build()
        }

    fun createProduct(products: CreateProductsEvent) =
        products.list.validate().run {
            val savedProducts = productRepository.saveAll(toEntityList())
            logger.info("Saved Products $savedProducts")
            savedProducts.toCreateProductResponseList()
        }

    fun receiveProducts(request: Proto.ProductNumberList) = request.productNumberList.run {
        val productsToReceive = productRepository
            .findAllByProductNumberIn(this)
            .map { it.copy(status = ProductStatus.RECEIVED) }
        val productsReceived = productRepository.saveAllAndFlush(productsToReceive)
        logger.info("Received Products $productsReceived")
        val productsProto = productsReceived.map {
            Proto.ReceiveProductItemDTO.newBuilder().setProductNumber(it.productNumber)
                .setStatus(Proto.ProductStatusDTO.valueOf(it.status.name)).build()
        }
        return@run Proto.ReceiveProductResponse.newBuilder().addAllProducts(productsProto).build()
    }

    fun deliverProducts(request: Proto.DeliverProductsDTO) =
        request.itemsList.validate().run {
            val idsToQuantity = associate { it.productNumber to it.quantity }
            val toUpdate = productRepository
                .findAllByProductNumberIn(this.map { it.productNumber })
                .map {
                    if (it.quantity < (idsToQuantity[it.productNumber] ?: -1)) {
                        throw NotEnoughQuantityProductException(
                            "Not enough available quantity of product with product number: ${it.productNumber}"
                        )
                    }
                    it.copy(quantity = it.quantity.minus(idsToQuantity[it.productNumber] ?: -1))
                }

            val productsDelivered = productRepository.saveAllAndFlush(toUpdate)
            logger.info("Delivered Products $productsDelivered")
            val productProto = productsDelivered.map {
                Proto.Item.newBuilder().setProductNumber(it.productNumber).setQuantity(it.quantity).build()
            }
            return@run Proto.DeliverProductsDTO.newBuilder().addAllItems(productProto).build()
        }

    fun getProductPrice(productNumbers: List<String>): Map<String, BigDecimal> =
        productNumbers.validate().run {
            val productsPrice = productRepository.findAllByProductNumberInOrderByCreatedAtDesc(productNumbers)
                .associate { (it.productNumber to it.price) }
            logger.info("Products price $productsPrice")
            return@run productsPrice
        }
}
