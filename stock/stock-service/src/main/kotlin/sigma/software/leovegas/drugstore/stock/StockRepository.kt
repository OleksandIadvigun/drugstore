package sigma.software.leovegas.drugstore.stock

import java.util.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository


data class StockView(val id: Long = -1, val quantity: Int)

@Repository
interface StockRepository: JpaRepository<Stock, Long>{

    fun findByProductId(id: Long?): Optional<Stock>

    @Query(
        """
            SELECT NEW sigma.software.leovegas.drugstore.stock.StockView(
                            id,
                            quantity
                    )
            FROM #{#entityName}   
                where id IN (:ids)
             """
    )
    fun findStockView(@Param("ids")  ids: Collection<Long>): List<StockView>
}
