package sigma.software.leovegas.drugstore.accountancy.schedule

import java.time.LocalDateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import sigma.software.leovegas.drugstore.accountancy.AccountancyService

@Component
class InvoiceCancellingJob(
    @Autowired val accountancyService: AccountancyService,
    @Value("\${accountancy.invoice.time-to-live-days}") val ttl: String,
) {

    @Scheduled(cron = "\${accountancy.invoice.scheduled-cron-expression}")
    fun cancelExpiredOrders() {
        accountancyService.cancelExpiredInvoice(LocalDateTime.now().minusDays(ttl.toLong()))
    }
}
