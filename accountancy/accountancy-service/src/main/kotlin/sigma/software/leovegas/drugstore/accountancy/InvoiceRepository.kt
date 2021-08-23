package sigma.software.leovegas.drugstore.accountancy

import java.util.*
import org.springframework.data.jpa.repository.JpaRepository

interface InvoiceRepository : JpaRepository<Invoice, Long> {

    fun getInvoiceByOrderId(orderId: Long): Optional<Invoice>
}
