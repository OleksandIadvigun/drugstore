package sigma.software.leovegas.drugstore.store.client

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties("store")
class StoreProperties(
    val host: String = "undefined",
    val port: Int = 80,
)
