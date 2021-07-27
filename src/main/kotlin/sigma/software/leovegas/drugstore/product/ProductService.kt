package sigma.software.leovegas.drugstore.product

import javax.transaction.Transactional
import org.springframework.beans.BeanUtils
import org.springframework.stereotype.Service

@Service
@Transactional
class ProductService(private val repo: ProductRepository) {

    companion object {
        private const val exceptionMessage = "This product with id: %d doesn't exist!"
    }

    fun getAll(): MutableList<ProductResponse> = repo.findAll().convertToProductResponseList()

    fun create(productRequest: ProductRequest): ProductResponse {
        return repo.save(productRequest.convertToProduct()).convertToProductResponse()
    }

    fun getOne(id: Long): ProductResponse {
        val product =
            repo.findById(id).orElseThrow { throw ResourceNotFoundException(String.format(exceptionMessage, id)) }
        return product.convertToProductResponse()
    }

    fun update(id: Long, productRequest: ProductRequest): ProductResponse {
        val product =
            repo.findById(id).orElseThrow { throw ResourceNotFoundException(String.format(exceptionMessage, id)) }
        BeanUtils.copyProperties(productRequest, product, "id")
        return product.convertToProductResponse()
    }

    fun delete(id: Long) {
        getOne(id)
        repo.deleteById(id)
    }
}
