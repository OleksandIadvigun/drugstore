package sigma.software.leovegas.drugstore.product

import java.util.Optional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.query.Param

interface ProductRepository : JpaRepository<Product, Long> {

    fun findFirstByIdOrderByCreatedAtDesc(@Param("id") id: Long): Optional<Product>

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
