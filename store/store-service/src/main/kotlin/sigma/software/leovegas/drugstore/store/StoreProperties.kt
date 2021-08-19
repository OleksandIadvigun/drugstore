package sigma.software.leovegas.drugstore.store

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties("store")
data class StoreProperties(
    val host: String = "undefined",
    val port: Int = 80,
)
