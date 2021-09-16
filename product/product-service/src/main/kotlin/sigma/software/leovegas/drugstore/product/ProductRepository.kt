package sigma.software.leovegas.drugstore.product

import java.util.Optional
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.query.Param

interface ProductRepository : JpaRepository<Product, Long> {

    fun findFirstByIdOrderByCreatedAtDesc(
        @Param("id") id: Long
    ): Optional<Product>

    fun findAllByProductNumberInOrderByCreatedAtDesc(productNumbers: List<String>): List<Product>

    fun findAllByProductNumberInAndStatus(productNumber: List<String>, status: ProductStatus): List<Product>

    fun findAllByNameContainingAndStatusAndQuantityGreaterThan(
        name: String?, status: ProductStatus, quantity: Int, pageable: Pageable?
    ): List<Product>

    fun findAllByNameContainingAndProductNumberInAndStatusAndQuantityGreaterThan(
        search: String,
        productNumbers: Set<String>,
        status: ProductStatus,
        quantity: Int,
        pageable: Pageable?
    ): List<Product>

    fun findAllByProductNumberInAndStatusAndQuantityGreaterThan(
        productNumbers: Set<String>, status: ProductStatus, quantity: Int, pageable: Pageable
    ): List<Product>

    fun findAllByProductNumberIn(productNumbers: List<String>): List<Product>
}
