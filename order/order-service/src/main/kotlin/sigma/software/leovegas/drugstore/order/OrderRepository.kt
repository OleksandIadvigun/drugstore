package sigma.software.leovegas.drugstore.order

import java.util.Optional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

data class View(val productNumber: String = "undefined", val quantity: Int = -1)

@Repository
interface OrderRepository : JpaRepository<Order, Long> {

    @Query(
        """
        select new sigma.software.leovegas.drugstore.order.View(productNumber, cast(sum(quantity) int))
        From OrderItem
        where productNumber in (:productNumbers) 
        group by productNumber 
        order by sum(quantity) DESC
        """
    )
    fun getProductNumberToQuantity(@Param("productNumbers") productNumbers: List<String>): List<View>

    fun findByOrderNumber(orderNumber: String): Optional<Order>

    fun getAllByOrderStatus(orderStatus: OrderStatus): List<Order>
}
