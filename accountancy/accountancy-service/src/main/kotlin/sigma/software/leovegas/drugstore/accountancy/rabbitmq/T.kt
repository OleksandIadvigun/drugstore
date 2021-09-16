package sigma.software.leovegas.drugstore.accountancy.rabbitmq

import java.util.function.Consumer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import sigma.software.leovegas.drugstore.accountancy.AccountancyService
import sigma.software.leovegas.drugstore.accountancy.Invoice
import sigma.software.leovegas.drugstore.product.api.CreateProductRequest

@Configuration
class T {

    val logger: Logger = LoggerFactory.getLogger(T::class.java)

    @Bean
    fun consumer() = Consumer<List<CreateProductRequest>> {event ->
            logger.info(" WORK OUR EVENT!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! $event")
    }
}