package sigma.software.leovegas.drugstore.accountancy

import java.math.BigDecimal
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.support.TransactionTemplate

@DisplayName("Invoice Repository test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class InvoiceRepositoryTest @Autowired constructor(
    val transactionTemplate: TransactionTemplate,
    val invoiceRepository: InvoiceRepository
) {

    @Test
    fun `should get invoice by order id`() {

        // given
        transactionTemplate.execute {
            invoiceRepository.deleteAll()
        }

        // and
        val created = transactionTemplate.execute {
            invoiceRepository.save(
                Invoice(
                    orderId = 1,
                    total = BigDecimal("10.00"),
                    status = InvoiceStatus.CREATED,
                )
            )
        } ?: fail("result is expected")

        // when
        val actual = invoiceRepository.getInvoiceByOrderId(created.orderId ?: -1).get()

        // then
        assertThat(actual.id).isEqualTo(created.id)
        assertThat(actual.total).isEqualTo(created.total)
        assertThat(actual.status).isEqualTo(created.status)

    }
}
