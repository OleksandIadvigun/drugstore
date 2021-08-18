package sigma.software.leovegas.drugstore.product.client

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties("product")
data class ProductProperties(
    val host: String = "undefined",
    val port: Int = 80,
)
