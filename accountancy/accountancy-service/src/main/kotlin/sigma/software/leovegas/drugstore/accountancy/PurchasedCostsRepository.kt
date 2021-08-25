package sigma.software.leovegas.drugstore.accountancy

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PurchasedCostsRepository : JpaRepository<PurchasedCosts, Long>
