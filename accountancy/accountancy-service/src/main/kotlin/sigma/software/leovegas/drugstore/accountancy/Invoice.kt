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
import javax.validation.constraints.DecimalMin
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull
import org.hibernate.annotations.CreationTimestamp

enum class InvoiceStatus {
    NONE,
    CREATED,
    CANCELLED,
    PAID,
    REFUND
}

enum class InvoiceType {
    NONE,
    INCOME,
    OUTCOME
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
    @Column(name = "invoice_number", nullable = false)
    val invoiceNumber: String = "undefined",

    @NotNull
    @Column(name = "order_number", nullable = false)
    val orderNumber: String = "undefined",

    @NotNull
    @DecimalMin("0.01")
    @Column(name = "total")
    val total: BigDecimal = BigDecimal.ZERO,

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    val type: InvoiceType = InvoiceType.NONE,

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    val status: InvoiceStatus = InvoiceStatus.NONE,

    @NotEmpty
    @JoinColumn(name = "invoice_id")
    @OneToMany(cascade = [CascadeType.ALL], fetch = FetchType.EAGER, orphanRemoval = true)
    val productItems: Set<ProductItem> = setOf(),

    @NotNull
    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    val createdAt: LocalDateTime? = null,
)
