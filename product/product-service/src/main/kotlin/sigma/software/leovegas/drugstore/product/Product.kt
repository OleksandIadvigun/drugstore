package sigma.software.leovegas.drugstore.product

import java.math.BigDecimal
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType.IDENTITY
import javax.persistence.Id
import javax.persistence.Table
import javax.validation.constraints.DecimalMax
import javax.validation.constraints.DecimalMin
import javax.validation.constraints.Min
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp

enum class ProductStatus {
    CREATED, RECEIVED
}

@Entity
@Table(name = "product")
data class Product(
    @Id
    @NotNull
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    val id: Long? = null,

    @NotEmpty
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    val status: ProductStatus = ProductStatus.CREATED,

    @NotEmpty
    @Column(name = "name", nullable = false, updatable = false)
    val name: String,

    @NotNull
    @DecimalMin("0")
    @DecimalMax("100000000")
    @Column(name = "price", updatable = false)
    val price: BigDecimal = BigDecimal.ZERO,

    @NotNull
    @Min(1)
    @Column(name = "quantity", nullable = false)
    val quantity: Int = 0,

    @NotNull
    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    val createdAt: LocalDateTime? = null,

    @NotNull
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime? = null
)
