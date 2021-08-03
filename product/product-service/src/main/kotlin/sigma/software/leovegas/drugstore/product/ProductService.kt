package sigma.software.leovegas.drugstore.product

import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
@Transactional
class ProductService(private val repo: ProductRepository) {

    companion object {
        private const val exceptionMessage = "This product with id: %d doesn't exist!"
    }

    fun getAll(): MutableList<ProductResponse> = repo.findAll().convertToProductResponseList()

    fun create(productRequest: ProductRequest): ProductResponse {
        productRequest.validate()
        return repo.save(productRequest.convertToProduct()).convertToProductResponse()
    }

    fun getOne(id: Long): ProductResponse {
        val product =
            repo.findById(id).orElseThrow { throw ResourceNotFoundException(String.format(exceptionMessage, id)) }
        return product.convertToProductResponse()
    }

    fun update(id: Long, productRequest: ProductRequest): ProductResponse {
        productRequest.validate()
        repo.findById(id).orElseThrow { throw ResourceNotFoundException(String.format(exceptionMessage, id)) }
        return repo.save(productRequest.convertToProduct().copy(id = id)).convertToProductResponse()
    }

    fun delete(id: Long) {
        getOne(id)
        repo.deleteById(id)
    }
}
