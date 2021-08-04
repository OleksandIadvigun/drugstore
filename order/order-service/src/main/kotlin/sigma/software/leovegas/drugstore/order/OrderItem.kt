package sigma.software.leovegas.drugstore.order

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "order_item")
data class OrderItem(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Long? = null,

    @Column(name = "product_id")
    val productId: Long,

    @Column(name = "quantity")
    val quantity: Int,
)
