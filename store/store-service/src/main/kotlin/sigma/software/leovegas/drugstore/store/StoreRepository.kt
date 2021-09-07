package sigma.software.leovegas.drugstore.store

import java.util.Optional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface StoreRepository : JpaRepository<TransferCertificate, Long> {

    fun findAllByOrderId(orderId: Long): List<TransferCertificate>

    fun getTransferCertificateByOrderId(orderId: Long): Optional<TransferCertificate>

}
