package sigma.software.leovegas.drugstore.stock

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "stock")
data class Stock(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Long? = null,

    @Column(name = "product_id")
    val productId: Long? = null,

    @Column(name = "quantity")
    val quantity: Int,
)
