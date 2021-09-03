package sigma.software.leovegas.drugstore.product

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface ProductRepository : JpaRepository<Product, Long> {

    fun findAllByNameContainingAndStatusAndQuantityGreaterThan(
        name: String?, status: ProductStatus, quantity: Int, pageable: Pageable?
    ): Page<Product>

    fun findAllByNameContainingAndIdInAndStatusAndQuantityGreaterThan(
        search: String,
        ids: Set<Long>,
        status: ProductStatus,
        quantity: Int,
        pageable: Pageable?
    ): Page<Product>

    fun findAllByIdInAndStatusAndQuantityGreaterThan(
        ids: Set<Long>, status: ProductStatus, quantity: Int, pageable: Pageable?
    ): Page<Product>
}
