package sigma.software.leovegas.drugstore.infrastructure

import org.junit.jupiter.api.DisplayName
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock

@DisplayName("WireMock test")
@AutoConfigureWireMock(port = 8080)
@SpringBootTest(webEnvironment = RANDOM_PORT)
abstract class WireMockTest
