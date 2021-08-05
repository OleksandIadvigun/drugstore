package sigma.software.leovegas.drugstore.product

import java.math.BigDecimal
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType.IDENTITY
import javax.persistence.Id
import javax.persistence.Table
import javax.validation.constraints.DecimalMax
import javax.validation.constraints.DecimalMin
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

@Entity
@Table(name = "product")
data class Product(
    @Id
    @NotNull
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    val id: Long? = null,

    @NotEmpty
    @Column(name = "name", nullable = false)
    val name: String,

    @NotNull
    @DecimalMin("0")
    @DecimalMax("100000000")
    @Column(name = "price")
    val price: BigDecimal
)

