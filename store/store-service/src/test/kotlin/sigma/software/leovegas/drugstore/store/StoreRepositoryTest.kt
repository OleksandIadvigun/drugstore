package sigma.software.leovegas.drugstore.store

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.infrastructure.extensions.get

@DisplayName("Store Repository test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class StoreRepositoryTest(
    @Autowired val transactionalTemplate: TransactionTemplate,
    @Autowired val storeRepository: StoreRepository,
) {

    @Test
    fun `should get transfer certificates by order number`() {

        // given
        transactionalTemplate.execute {
            storeRepository.deleteAll()
        }

        // and
        val created = transactionalTemplate.execute {
            storeRepository.save(
                TransferCertificate(
                    certificateNumber = "1",
                    orderNumber = "1",
                    status = TransferStatus.DELIVERED,
                    comment = "Delivered"
                )
            )
        }.get()

        // when
        val found = transactionalTemplate.execute {
            storeRepository.findAllByOrderNumber("1")
        }.get()

        // then
        assertThat(found[0].orderNumber).isEqualTo(created.orderNumber)
    }

    @Test
    fun `should get transfer certificate by order number`() {

        // given
        transactionalTemplate.execute {
            storeRepository.deleteAll()
        }

        // and
        val created = transactionalTemplate.execute {
            storeRepository.save(
                TransferCertificate(
                    certificateNumber = "1",
                    orderNumber = "1",
                    status = TransferStatus.DELIVERED,
                    comment = "Delivered"
                )
            )
        }.get()

        // when
        val found = transactionalTemplate.execute {
            storeRepository.getTransferCertificateByOrderNumber("1")
        }.get()

        // then
        assertThat(found.get().orderNumber).isEqualTo(created.orderNumber)
    }
}
