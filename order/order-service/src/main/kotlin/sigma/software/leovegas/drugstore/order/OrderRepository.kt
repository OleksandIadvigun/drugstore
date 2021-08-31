package sigma.software.leovegas.drugstore.order

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

data class View(val priceItemId: Long = -1, val quantity: Int = -1)

@Repository
interface OrderRepository : JpaRepository<Order, Long> {
    fun getAllByOrderStatus(orderStatus: OrderStatus): List<Order>

    @Query(
        """
      select new sigma.software.leovegas.drugstore.order.View(priceItemId, cast(sum(quantity) int))
From OrderItem where id in (:ids) group by priceItemId order by sum(quantity) DESC
    """
    )
    fun getIdToQuantity(@Param("ids") ids: List<Long>): List<View>
}
