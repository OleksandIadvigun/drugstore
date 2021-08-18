package sigma.software.leovegas.drugstore

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import sigma.software.leovegas.drugstore.order.OrderProperties

@EnableConfigurationProperties(OrderProperties::class)
@SpringBootApplication
class OrderServiceApplication

fun main(args: Array<String>) {
    runApplication<OrderServiceApplication>(*args)
}
