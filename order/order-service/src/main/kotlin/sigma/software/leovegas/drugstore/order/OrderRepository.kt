package sigma.software.leovegas.drugstore.order

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

data class View(val productId: Long = -1, val quantity: Int = -1)

@Repository
interface OrderRepository : JpaRepository<Order, Long> {
    fun getAllByOrderStatus(orderStatus: OrderStatus): List<Order>

    @Query(
        """
      select new sigma.software.leovegas.drugstore.order.View(productId, cast(sum(quantity) int))
From OrderItem group by productId order by sum(quantity) DESC
    """
    )
    fun getIdToQuantity(): List<View>
}
