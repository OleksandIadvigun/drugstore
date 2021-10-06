package sigma.software.leovegas.drugstore.infrastructure

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock

@TestInstance(PER_CLASS)
@DisplayName("WireMock test")
@AutoConfigureWireMock(port = 8079)
@SpringBootTest(webEnvironment = RANDOM_PORT)
abstract class WireMockTest
