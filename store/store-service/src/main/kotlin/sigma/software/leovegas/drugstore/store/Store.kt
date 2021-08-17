package sigma.software.leovegas.drugstore.store

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table
import javax.validation.constraints.NotNull

@Entity
@Table(name = "store")
data class Store(

    @Id
    @NotNull
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    val id: Long? = null,

    @Column(name = "price_item_id", nullable = false)
    val priceItemId: Long = -1,

    @Column(name = "quantity", nullable = false)
    val quantity: Int = -1,
)
