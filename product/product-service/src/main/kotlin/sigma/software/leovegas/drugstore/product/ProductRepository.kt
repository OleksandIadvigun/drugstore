package sigma.software.leovegas.drugstore.product

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface ProductRepository : JpaRepository<Product, Long> {

    fun findAllByNameContainingAndStatus(name: String?, status: ProductStatus, pageable: Pageable?): Page<Product>

    fun findAllByNameContainingAndIdInAndStatus(
        search: String,
        ids: Set<Long>,
        status: ProductStatus,
        pageable: Pageable?
    ): Page<Product>

    fun findAllByIdInAndStatus(ids: Set<Long>, status: ProductStatus, pageable: Pageable?): Page<Product>
}
