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
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp

enum class OrderStatus {
    NONE,
    CREATED,
    CANCELLED,
    UPDATED,
    PAID,
    DELIVERED
}

@Entity
@Table(name = "order")
data class Order(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    val id: Long? = null,

    @Column(name="order_status")
    @Enumerated(EnumType.STRING)
    val orderStatus: OrderStatus = OrderStatus.NONE,

    @Column(name="created_at")
    @CreationTimestamp
    val createdAt: LocalDateTime? = null,

    @Column(name="updated_at")
    @UpdateTimestamp
    val updatedAt: LocalDateTime? =null,

    @JoinColumn(name = "order_id")
    @OneToMany(cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    val orderItems: Set<OrderItem> = setOf()
)

