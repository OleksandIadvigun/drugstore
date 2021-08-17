package sigma.software.leovegas.drugstore.store

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface StoreRepository : JpaRepository<Store, Long> {

    @Query("""select s FROM Store s where s.priceItemId IN (:ids) """)
    fun getStoreByPriceItemIds(@Param("ids") ids: List<Long>): List<Store>
}
