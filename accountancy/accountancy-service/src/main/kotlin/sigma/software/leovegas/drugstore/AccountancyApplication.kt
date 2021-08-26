package sigma.software.leovegas.drugstore

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication
class AccountancyApplication

fun main(args: Array<String>) {
    runApplication<AccountancyApplication>(*args)
}
