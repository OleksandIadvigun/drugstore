package sigma.software.leovegas.drugstore

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import sigma.software.leovegas.drugstore.store.StoreProperties

@SpringBootApplication
@EnableConfigurationProperties(StoreProperties::class)
class StoreApplication

fun main(args: Array<String>) {
    runApplication<StoreApplication>(*args)
}
