package sigma.software.leovegas.drugstore.order

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table
import sigma.software.leovegas.drugstore.product.Product

@Entity
@Table(name = "order_details")
data class OrderDetails(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Long? = null,

    @ManyToOne()
    @JoinColumn(name = "product_id")
    val product: Product,

    @Column(name = "quantity")
    val quantity: Int,
)
