package sigma.software.leovegas.drugstore.product

import java.util.Optional
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.query.Param

interface ProductRepository : JpaRepository<Product, Long> {

    fun findFirstByIdOrderByCreatedAtDesc(
        @Param("id") id: Long
    ): Optional<Product>

    fun findAllByIdInOrderByCreatedAtDesc(ids: List<Long>): List<Product>

    fun findAllByIdInAndStatus(ids: List<Long>, status: ProductStatus): List<Product>

    fun findAllByNameContainingAndStatusAndQuantityGreaterThan(
        name: String?, status: ProductStatus, quantity: Int, pageable: Pageable?
    ): List<Product>

    fun findAllByNameContainingAndIdInAndStatusAndQuantityGreaterThan(
        search: String,
        ids: Set<Long>,
        status: ProductStatus,
        quantity: Int,
        pageable: Pageable?
    ): List<Product>

    fun findAllByIdInAndStatusAndQuantityGreaterThan(
        ids: Set<Long>, status: ProductStatus, quantity: Int, pageable: Pageable?
    ): List<Product>
}
