package sigma.software.leovegas.drugstore.product

import java.math.BigDecimal
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

data class View(val productId: Long = -1, val price: BigDecimal = BigDecimal.ZERO)

@Repository
interface ProductRepository : JpaRepository<Product, Long> {


    @Query(
        """
            SELECT NEW sigma.software.leovegas.drugstore.product.View(
                            id as productId,
                            price
                    )
            FROM #{#entityName}   
                where id IN (:ids)
             """
    )
    fun findProductsView(@Param("ids")  ids: Collection<Long>): List<View>
}

