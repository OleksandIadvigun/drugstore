package sigma.software.leovegas.drugstore.accountancy

import java.math.BigDecimal
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table
import javax.validation.constraints.DecimalMax
import javax.validation.constraints.DecimalMin
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull

@Entity
@Table(name = "product_item")
data class ProductItem(
    @Id
    @NotNull
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    val id: Long? = null,

    @NotNull
    @Column(name = "price_item_id", nullable = false)
    val priceItemId: Long? = null,

    @NotNull
    @Column(name = "name", nullable = false)
    val name: String = "default",

    @NotNull
    @DecimalMin("0")
    @DecimalMax("100000000")
    @Column(name = "price")
    val price: BigDecimal = BigDecimal.ZERO,

    @NotNull
    @Min(1)
    @Column(name = "quantity", nullable = false)
    val quantity: Int = 0,
)