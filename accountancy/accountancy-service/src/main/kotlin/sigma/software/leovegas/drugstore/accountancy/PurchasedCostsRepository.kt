package sigma.software.leovegas.drugstore.accountancy

import java.time.LocalDateTime
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PurchasedCostsRepository : JpaRepository<PurchasedCosts, Long> {
    fun findAllByDateOfPurchaseIsBefore(date: LocalDateTime): List<PurchasedCosts>

    fun findAllByDateOfPurchaseIsAfter(date: LocalDateTime): List<PurchasedCosts>

    fun findAllByDateOfPurchaseIsBetween(dateFrom: LocalDateTime, dateTo: LocalDateTime): List<PurchasedCosts>
}
