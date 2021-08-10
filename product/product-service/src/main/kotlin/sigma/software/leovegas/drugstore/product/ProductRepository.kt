package sigma.software.leovegas.drugstore.product

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ProductRepository : JpaRepository<Product, Long> {

    @Query(
        value = "SELECT p FROM Product p WHERE p.name like %?1% OR CONCAT(p.price,'') LIKE %?1%",
        countQuery = "SELECT COUNT(p.id) FROM Product p WHERE p.name like %?1% OR CONCAT(p.price,'') LIKE %?1%"
    )
    fun findAll(search: String?, pageable: Pageable?): Page<Product>

    @Query(
        value = "SELECT p FROM Product p WHERE p.name like %?1% OR CONCAT(p.price,'') LIKE %?1%",
        countQuery = "SELECT COUNT(p.id) FROM Product p WHERE p.name like %?1% OR CONCAT(p.price,'') LIKE %?1%"
    )
    fun findAllById(keyword: String, ids: Set<Long>, pageable: Pageable?): Page<Product>
}

