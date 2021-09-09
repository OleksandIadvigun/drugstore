package sigma.software.leovegas.drugstore.store

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

enum class TransferStatus {
    NONE,
    RECEIVED,
    DELIVERED,
    RETURN,
    CLOSED
}

@Entity
@Table(name = "transfer_certificate")
data class TransferCertificate(

    @Id
    @NotNull
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    val id: Long? = null,

    @NotNull
    @Column(name = "order_number", nullable = false)
    val orderNumber: Long = -1,

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "store_status", nullable = false)
    val status: TransferStatus = TransferStatus.NONE,

    @NotEmpty
    @Column(name = "comment", nullable = false, updatable = false)
    val comment: String = "undefined",
)
