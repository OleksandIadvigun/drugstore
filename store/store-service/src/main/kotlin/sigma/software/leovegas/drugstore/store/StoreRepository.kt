package sigma.software.leovegas.drugstore.store

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface StoreRepository : JpaRepository<TransferCertificate, Long> {

    fun findAllByInvoiceId(invoiceId: Long): List<TransferCertificate>
}
