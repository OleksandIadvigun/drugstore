package sigma.software.leovegas.drugstore.accountancy

import java.util.function.Consumer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import sigma.software.leovegas.drugstore.accountancy.api.CreateOutcomeInvoiceEvent
import sigma.software.leovegas.drugstore.api.protobuf.Proto


@Configuration
class AccountancyEventHandler(
    val accountancyService: AccountancyService
) {

    @Bean
    fun createOutcomeInvoiceEventHandler() = Consumer<Proto.CreateOutcomeInvoiceEvent> {
        accountancyService.createOutcomeInvoice(it)
    }

}
