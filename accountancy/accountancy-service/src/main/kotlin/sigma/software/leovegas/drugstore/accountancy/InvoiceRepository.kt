package sigma.software.leovegas.drugstore.accountancy

import java.time.LocalDateTime
import java.util.Optional
import org.springframework.data.jpa.repository.JpaRepository

interface InvoiceRepository : JpaRepository<Invoice, Long> {

    fun getInvoiceByOrderNumber(orderNumber: String): Optional<Invoice>

    fun getInvoiceByOrderNumberAndStatusLike(orderNumber: String, invoiceStatus: InvoiceStatus): Optional<Invoice>

    fun getInvoiceByOrderNumberAndStatusNotLike(orderNumber: String, invoiceStatus: InvoiceStatus): Optional<Invoice>

    fun findAllByStatusAndCreatedAtLessThan(status: InvoiceStatus, expireDate: LocalDateTime): List<Invoice>

    fun findAllByStatus(status: InvoiceStatus): List<Invoice>

    fun getInvoiceByInvoiceNumber(invoiceNumber: String): Optional<Invoice>
}
