package sigma.software.leovegas.drugstore.order

import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToMany
import javax.persistence.Table

@Entity
@Table(name = "order")
data class Order(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @JoinColumn(name = "order_id")
    @OneToMany(cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    val orderItems: Set<OrderItem> = setOf()
)

