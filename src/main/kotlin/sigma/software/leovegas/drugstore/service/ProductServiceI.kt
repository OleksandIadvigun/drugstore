package sigma.software.leovegas.drugstore.service

import sigma.software.leovegas.drugstore.dto.ProductRequest
import sigma.software.leovegas.drugstore.dto.ProductResponse

interface ProductServiceI {

    fun getAll(): MutableList<ProductResponse>

    fun save(product: ProductRequest): ProductResponse

    fun getOne(id: Long): ProductResponse

    fun update(id: Long, product: ProductRequest): ProductResponse

    fun delete(id: Long)

}
