package sigma.software.leovegas.drugstore.service

import org.springframework.beans.BeanUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import sigma.software.leovegas.drugstore.dto.ProductRequest
import sigma.software.leovegas.drugstore.dto.ProductResponse
import sigma.software.leovegas.drugstore.exception.ResourceNotFoundException
import sigma.software.leovegas.drugstore.persistence.entity.convertToProduct
import sigma.software.leovegas.drugstore.persistence.entity.convertToProductResponse
import sigma.software.leovegas.drugstore.persistence.entity.convertToProductResponseList
import sigma.software.leovegas.drugstore.persistence.repository.ProductRepository
import javax.transaction.Transactional


@Service
@Transactional
class ProductServiceImpl : ProductServiceI {
    @Autowired
    lateinit var repo: ProductRepository
    val exceptionMessage = "This product with id: %d doesn't exist!"

    override fun getAll(): MutableList<ProductResponse> = repo.findAll().convertToProductResponseList()

    override fun save(productRequest: ProductRequest): ProductResponse {
        return repo.save(productRequest.convertToProduct()).convertToProductResponse()
    }

    override fun getOne(id: Long): ProductResponse {
        val product =
            repo.findById(id).orElseThrow { throw ResourceNotFoundException(String.format(exceptionMessage, id)) }
        return product.convertToProductResponse()
    }

    override fun update(id: Long, productRequest: ProductRequest): ProductResponse {
        val product =
            repo.findById(id).orElseThrow { throw ResourceNotFoundException(String.format(exceptionMessage, id)) }
        BeanUtils.copyProperties(productRequest, product, "id")
        return product.convertToProductResponse()
    }

    override fun delete(id: Long) {
        getOne(id)
        repo.deleteById(id)
    }

}
