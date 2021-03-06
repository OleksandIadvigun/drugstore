package sigma.software.leovegas.drugstore.store.restdoc

import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.infrastructure.extensions.get
import sigma.software.leovegas.drugstore.store.StoreProperties
import sigma.software.leovegas.drugstore.store.StoreRepository
import sigma.software.leovegas.drugstore.store.TransferCertificate
import sigma.software.leovegas.drugstore.store.TransferStatus

@DisplayName("Get transfer certificates by order number REST API Doc test")
class RestApiDocGetTransferCertificatesByOrderNumber @Autowired constructor(
    val transactionTemplate: TransactionTemplate,
    val storeProperties: StoreProperties,
    val storeRepository: StoreRepository,
    @LocalServerPort val port: Int,
) : RestApiDocumentationTest(storeProperties) {

    @Test
    fun `should get transfer certificate by order number`() {

        // given
        transactionTemplate.execute {
            storeRepository.deleteAllInBatch()
        }

        val orderNumber = "1"

        // and
        transactionTemplate.execute {
            storeRepository.save(
                TransferCertificate(
                    orderNumber = orderNumber,
                    status = TransferStatus.RECEIVED,
                    comment = "RECEIVED"
                )
            )
        }.get()

        of("get-transfer-certificates-by-order-number").`when`()
            .get("http://${storeProperties.host}:$port/api/v1/store/transfer-certificate/order/$orderNumber")
            .then()
            .assertThat().statusCode(200)
            .assertThat().body("[0].orderNumber", equalTo("1"))
    }
}
