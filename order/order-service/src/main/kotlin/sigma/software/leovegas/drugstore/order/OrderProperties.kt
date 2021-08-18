package sigma.software.leovegas.drugstore.order

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties("order")
data class OrderProperties(
    val host: String = "undefined",
    val port: Int = 80,
)
