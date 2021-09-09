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

@DisplayName("Get transfer certificates by order id REST API Doc test")
class RestApiDocGetTransferCertificatesByOrderId @Autowired constructor(
    @LocalServerPort val port: Int,
    val storeProperties: StoreProperties,
    val storeRepository: StoreRepository,
    val transactionTemplate: TransactionTemplate
) : RestApiDocumentationTest(storeProperties) {

    @Test
    fun `should get transfer certificate by order id`() {

        // given
        transactionTemplate.execute {
            storeRepository.deleteAllInBatch()
        }

        val orderId: Long = 1

        // and
        transactionTemplate.execute {
            storeRepository.save(
                TransferCertificate(
                    orderId = orderId,
                    status = TransferStatus.RECEIVED,
                    comment = "RECEIVED"
                )
            )
        }.get()

        of("get-transfer-certificates-by-order-id").`when`()
            .get("http://${storeProperties.host}:$port/api/v1/store/transfer-certificate/order/$orderId")
            .then()
            .assertThat().statusCode(200)
            .assertThat().body("[0].orderId", equalTo(1))
    }
}
