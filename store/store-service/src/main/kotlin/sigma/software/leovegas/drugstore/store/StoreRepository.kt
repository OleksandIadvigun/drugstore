package sigma.software.leovegas.drugstore.store

import java.util.Optional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface StoreRepository : JpaRepository<TransferCertificate, Long> {

    fun findAllByOrderNumber(orderNumber: String): List<TransferCertificate>

    fun getTransferCertificateByOrderNumber(orderNumber: String): Optional<TransferCertificate>

}
