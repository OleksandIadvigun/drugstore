package sigma.software.leovegas.drugstore.product

import java.math.BigDecimal
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType.IDENTITY
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "product")
data class Product(
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id")
    val id: Long? = null,

    @Column(name = "name")
    var name: String,

    @Column(name = "quantity")
    var quantity: Int,

    @Column(name = "price")
    var price: BigDecimal

)

