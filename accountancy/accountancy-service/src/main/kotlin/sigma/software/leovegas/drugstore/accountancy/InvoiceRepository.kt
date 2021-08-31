package sigma.software.leovegas.drugstore.accountancy

import java.time.LocalDateTime
import java.util.Optional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import sigma.software.leovegas.drugstore.accountancy.api.PurchasedItemDTO

interface InvoiceRepository : JpaRepository<Invoice, Long> {

    fun getInvoiceByOrderId(orderId: Long): Optional<Invoice>

    fun findAllByStatusAndCreatedAtLessThan(status: InvoiceStatus, expireDate: LocalDateTime): List<Invoice>

    fun findAllByStatus(status: InvoiceStatus): List<Invoice>

    @Query(
        """
        select new sigma.software.leovegas.drugstore.accountancy.api.PurchasedItemDTO(p.name, p.price, cast(sum(p.quantity) int)) 
        From ProductItem p where p.id in (:ids) group by p.priceItemId,p.name,p.price
        """
    )
    fun getPurchasedItems(@Param("ids") ids: List<Long>): List<PurchasedItemDTO>
}
