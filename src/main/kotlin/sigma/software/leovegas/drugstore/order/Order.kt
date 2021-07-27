package sigma.software.leovegas.drugstore.order

import java.math.BigDecimal
import javax.persistence.CascadeType
import javax.persistence.Column
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
    var orderDetailsList: List<OrderDetails>? = listOf(),

    @Column(name = "total")
    var total: BigDecimal? = null,
)

