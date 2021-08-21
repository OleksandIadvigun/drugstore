package sigma.software.leovegas.drugstore.accountancy

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface PriceItemRepository : JpaRepository<PriceItem, Long> {

    @Query("""select p FROM PriceItem p where p.productId IN (:ids)""")
    fun findAllByProductId(ids: List<Long>): List<PriceItem>
}

