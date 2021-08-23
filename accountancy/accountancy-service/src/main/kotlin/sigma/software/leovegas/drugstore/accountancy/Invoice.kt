package sigma.software.leovegas.drugstore.accountancy

import java.math.BigDecimal
import java.time.LocalDateTime
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToMany
import javax.persistence.Table
import javax.validation.constraints.DecimalMax
import javax.validation.constraints.DecimalMin
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull
import org.hibernate.annotations.CreationTimestamp

enum class InvoiceStatus {
    CREATED,
    CANCELLED,
    PAID,
    REFUND
}

@Entity
@Table(name = "invoice")
data class Invoice(
    @Id
    @NotNull
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    val id: Long? = null,

    @NotNull
    @Column(name = "order_id", nullable = false)
    val orderId: Long? = null,

    @NotNull
    @DecimalMin("0")
    @DecimalMax("100000000")
    @Column(name = "total")
    val total: BigDecimal = BigDecimal.ZERO,

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    val status: InvoiceStatus = InvoiceStatus.CREATED,

    @NotEmpty
    @JoinColumn(name = "invoice_id")
    @OneToMany(cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    val productItems: Set<ProductItem> = setOf(),

    @NotNull
    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    val createdAt: LocalDateTime? = null,
)
