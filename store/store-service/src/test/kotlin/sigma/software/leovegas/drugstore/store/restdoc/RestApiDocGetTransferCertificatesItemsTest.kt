package sigma.software.leovegas.drugstore.store.restdoc

import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.store.StoreProperties
import sigma.software.leovegas.drugstore.store.StoreRepository
import sigma.software.leovegas.drugstore.store.TransferCertificate
import sigma.software.leovegas.drugstore.store.TransferStatus

@DisplayName("Get transfer certificates REST API Doc test")
class RestApiDocGetTransferCertificatesItemsTest @Autowired constructor(
    val transactionTemplate: TransactionTemplate,
    val storeProperties: StoreProperties,
    val storeRepository: StoreRepository,
    @LocalServerPort val port: Int,
) : RestApiDocumentationTest(storeProperties) {

    @Test
    fun `should get transfer certificates`() {

        // given
        transactionTemplate.execute {
            storeRepository.deleteAllInBatch()
        }

        // and
        transactionTemplate.execute {
            storeRepository.saveAll(
                listOf(
                    TransferCertificate(
                        certificateNumber = "1",
                        orderNumber = "1",
                        status = TransferStatus.RECEIVED,
                        comment = "RECEIVED"
                    ),
                    TransferCertificate(
                        certificateNumber = "2",
                        orderNumber = "2",
                        status = TransferStatus.DELIVERED,
                        comment = "DELIVERED"
                    )
                )
            )
        }

        of("get-transfer-certificates").`when`()
            .get("http://${storeProperties.host}:$port/api/v1/store/transfer-certificate")
            .then()
            .assertThat().statusCode(200)
            .assertThat().body("[0].orderNumber", `is`("1"))
            .assertThat().body("size", `is`(2))
    }
}
