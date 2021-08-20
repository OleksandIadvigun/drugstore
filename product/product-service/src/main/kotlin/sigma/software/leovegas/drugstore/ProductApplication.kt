package sigma.software.leovegas.drugstore

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import sigma.software.leovegas.drugstore.product.ProductProperties

@EnableConfigurationProperties(ProductProperties::class)
@SpringBootApplication
class ProductApplication

fun main(args: Array<String>) {
    runApplication<ProductApplication>(*args)
}
