package sigma.software.leovegas.drugstore.order

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
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp

enum class OrderStatus {
    NONE,
    CREATED,
    CANCELLED,
    UPDATED,
    PAID,
    BOOKED,
    DELIVERED
}

@Entity
@Table(name = "order")
data class Order(

    @Id
    @NotNull
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    val id: Long? = null,

    @NotEmpty
    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false)
    val orderStatus: OrderStatus = OrderStatus.NONE,

    @NotEmpty // TODO: FIXME: Make sure its not possible to save an order without order items (write test)
    @JoinColumn(name = "order_id")
    @OneToMany(cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    val orderItems: Set<OrderItem> = setOf(),

    @NotNull
    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    val createdAt: LocalDateTime? = null,

    @NotNull
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime? = null,
)
