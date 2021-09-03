package sigma.software.leovegas.drugstore.store

import org.assertj.core.api.Assertions
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.transaction.support.TransactionTemplate

@DisplayName("Get transfer certificates by invoice id REST API Doc test")
class RestApiDocGetTransferCertificatesByInvoiceId @Autowired constructor(
    @LocalServerPort val port: Int,
    val storeProperties: StoreProperties,
    val storeRepository: StoreRepository,
    val transactionTemplate: TransactionTemplate
) : RestApiDocumentationTest(storeProperties) {

    @Test
    fun `should get transfer certificate by invoice id`() {

        // given
        transactionTemplate.execute {
            storeRepository.deleteAllInBatch()
        }

        // and
        transactionTemplate.execute {
            storeRepository.save(
                TransferCertificate(
                    invoiceId = 1,
                    status = TransferStatus.RECEIVED,
                    comment = "RECEIVED"
                )
            )
        } ?: Assertions.fail("result is expected")

        of("get-transfer-certificates-by-invoice-id").`when`()
            .get("http://${storeProperties.host}:$port/api/v1/store/transfer-certificate/invoice/1")
            .then()
            .assertThat().statusCode(200)
            .assertThat().body("size()", `is`(1))
            .assertThat().body("[0].invoiceId", equalTo(1))
    }
}
