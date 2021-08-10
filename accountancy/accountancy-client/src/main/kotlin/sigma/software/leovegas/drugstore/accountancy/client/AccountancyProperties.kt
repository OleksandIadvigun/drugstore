package sigma.software.leovegas.drugstore.accountancy.client

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties("accountancy")
data class AccountancyProperties(
    val host: String = "undefined",
    val port: Int = 80,
)
