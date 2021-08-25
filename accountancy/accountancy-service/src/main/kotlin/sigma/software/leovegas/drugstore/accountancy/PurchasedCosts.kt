package sigma.software.leovegas.drugstore.accountancy

import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table
import javax.validation.constraints.NotNull
import org.hibernate.annotations.CreationTimestamp

@Entity
@Table(name = "purchased_costs")
data class PurchasedCosts(
    @Id
    @NotNull
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    val id: Long? = null,

    @NotNull
    @Column(name = "price_item_id", nullable = false)
    val priceItemId: Long = -1,

    @NotNull
    @Column(name = "quantity", nullable = false)
    val quantity: Int = -1,

    @NotNull
    @CreationTimestamp
    @Column(name = "date_of_purchased", updatable = false, nullable = false)
    val dateOfPurchase: LocalDateTime? = null,
)
