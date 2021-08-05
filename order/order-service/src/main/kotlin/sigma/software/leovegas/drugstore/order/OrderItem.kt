package sigma.software.leovegas.drugstore.order


import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table
import javax.validation.constraints.NotNull

@Entity
@Table(name = "order_item")
data class OrderItem(

    @Id
    @NotNull
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id",nullable = false,updatable = false)
    val id: Long? = null,

    @NotNull
    @Column(name = "product_id",nullable = false)
    val productId: Long,

    @NotNull
    @Column(name = "quantity",nullable = false)
    val quantity: Int,
)
