package sigma.software.leovegas.drugstore.order.client

import org.junit.jupiter.api.DisplayName
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock

@DisplayName("WireMock test")
@AutoConfigureWireMock(port=8082)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class WireMockTest {
}
