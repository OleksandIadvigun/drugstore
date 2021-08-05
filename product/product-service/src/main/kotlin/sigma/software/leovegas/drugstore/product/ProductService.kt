package sigma.software.leovegas.drugstore.product

import javax.transaction.Transactional
import org.springframework.stereotype.Service
import sigma.software.leovegas.drugstore.product.api.ProductRequest
import sigma.software.leovegas.drugstore.product.api.ProductResponse

@Service
@Transactional
class ProductService(private val repo: ProductRepository) {

    companion object {
        private const val exceptionMessage = "This product with id: %d doesn't exist!"
    }

    fun getAll(): List<ProductResponse> = repo.findAll().toProductResponseList()

    fun create(productRequest: ProductRequest): ProductResponse = productRequest.run {
        repo.save(toEntity()).toProductResponse()
    }

    fun getOne(id: Long): ProductResponse =
        repo.findById(id).orElseThrow { throw ResourceNotFoundException(String.format(exceptionMessage, id)) }
            .toProductResponse()

    fun update(id: Long, productRequest: ProductRequest): ProductResponse = productRequest.run {
        repo.findById(id).orElseThrow { throw ResourceNotFoundException(String.format(exceptionMessage, id)) }
        repo.save(toEntity().copy(id = id)).toProductResponse()
    }

    fun delete(id: Long) {
        getOne(id)
        repo.deleteById(id)
    }
}
