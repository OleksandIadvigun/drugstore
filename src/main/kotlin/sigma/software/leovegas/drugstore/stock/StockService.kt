package sigma.software.leovegas.drugstore.stock

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import sigma.software.leovegas.drugstore.product.ProductRepository

@Service
@Transactional
class StockService @Autowired constructor(
    val stockRepo: StockRepository,
    val productRepo: ProductRepository
) {

    fun create(stockRequest: StockRequest): StockResponse {
        productRepo.findById(stockRequest.productId!!)
            .orElseThrow { ProductIsNotExistException(stockRequest.productId) }
        val isExistStockWithThisProduct = stockRepo.findByProductId(stockRequest.productId).isPresent
        if (isExistStockWithThisProduct) {
            throw StockWithThisProductAlreadyExistException()
        }
        return stockRepo.save(stockRequest.convertToStock()).convertToStockResponse()
    }

    fun getAll(): MutableList<StockResponse> = stockRepo.findAll().convertToListOfStockResponses()

    fun getOne(id: Long): StockResponse = stockRepo.findById(id).orElseThrow { StockNotFoundException(id) }
        .convertToStockResponse()

    fun update(id: Long, stockRequest: StockRequest): StockResponse {
        stockRepo.findById(id).orElseThrow { StockNotFoundException(id) }
        return stockRepo.save(stockRequest.convertToStock().copy(id = id)).convertToStockResponse()
    }

    fun delete(id: Long) {
        stockRepo.findById(id).orElseThrow { StockNotFoundException(id) }
        stockRepo.deleteById(id)
    }

}
