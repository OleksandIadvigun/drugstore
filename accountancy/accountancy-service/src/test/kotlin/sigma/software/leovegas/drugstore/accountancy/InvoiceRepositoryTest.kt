package sigma.software.leovegas.drugstore.accountancy

import java.math.BigDecimal
import java.time.LocalDateTime
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.support.TransactionTemplate
import sigma.software.leovegas.drugstore.extensions.get

@TestInstance(PER_CLASS)
@DisplayName("Invoice Repository test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class InvoiceRepositoryTest @Autowired constructor(
    val transactionTemplate: TransactionTemplate,
    val invoiceRepository: InvoiceRepository,
) {

    @Test
    fun `should get invoice by order number`() {

        // setup
        transactionTemplate.execute {
            invoiceRepository.deleteAll()
        }

        // given
        val created = transactionTemplate.execute {
            invoiceRepository.save(
                Invoice(
                    invoiceNumber = "1",
                    orderNumber = "1",
                    total = BigDecimal("10.00"),
                    status = InvoiceStatus.CREATED,
                )
            )
        }.get()

        // when
        val actual = invoiceRepository.getInvoiceByOrderNumber(created.orderNumber).get()

        // then
        assertThat(actual.id).isNotNull
        assertThat(actual.invoiceNumber).isEqualTo(created.invoiceNumber)
        assertThat(actual.orderNumber).isEqualTo(created.orderNumber)
        assertThat(actual.total).isEqualTo(created.total)
        assertThat(actual.status).isEqualTo(created.status)

    }

    @Test
    fun `should get invoice by status and createdAt less than`() {

        // setup
        transactionTemplate.execute {
            invoiceRepository.deleteAll()
        }

        // given
        val created = transactionTemplate.execute {
            invoiceRepository.saveAll(
                listOf(
                    Invoice(
                        invoiceNumber = "1",
                        orderNumber = "1",
                        total = BigDecimal("10.00"),
                        status = InvoiceStatus.CREATED,
                    ),
                    Invoice(
                        invoiceNumber = "2",
                        orderNumber = "2",
                        total = BigDecimal("10.00"),
                        status = InvoiceStatus.PAID,
                    )
                )
            )
        }.get()

        // when
        val actual = invoiceRepository.findAllByStatusAndCreatedAtLessThan(
            InvoiceStatus.CREATED,
            LocalDateTime.now().plusDays(1)
        )

        // then
        assertThat(actual).hasSize(1)
        assertThat(actual[0].invoiceNumber).isEqualTo(created[0].invoiceNumber)
        assertThat(actual[0].orderNumber).isEqualTo("1")
        assertThat(actual[0].total).isEqualTo(BigDecimal("10.00"))
        assertThat(actual[0].status).isEqualTo(InvoiceStatus.CREATED)
    }

    @Test
    fun `should get invoice by status`() {

        // setup
        transactionTemplate.execute {
            invoiceRepository.deleteAll()
        }

        // given
        val created = transactionTemplate.execute {
            invoiceRepository.saveAll(
                listOf(
                    Invoice(
                        invoiceNumber = "1",
                        orderNumber = "1",
                        total = BigDecimal("10.00"),
                        status = InvoiceStatus.CREATED,
                    ),
                    Invoice(
                        invoiceNumber = "2",
                        orderNumber = "2",
                        total = BigDecimal("10.00"),
                        status = InvoiceStatus.PAID,
                    )
                )
            )
        }.get()

        // when
        val actual = invoiceRepository.findAllByStatus(InvoiceStatus.CREATED)

        // then
        assertThat(actual).hasSize(1)
        assertThat(actual[0].invoiceNumber).isEqualTo(created[0].invoiceNumber)
        assertThat(actual[0].orderNumber).isEqualTo("1")
        assertThat(actual[0].total).isEqualTo(BigDecimal("10.00"))
        assertThat(actual[0].status).isEqualTo(InvoiceStatus.CREATED)
    }

    @Test
    fun `should get invoice by invoice number`() {

        // setup
        transactionTemplate.execute {
            invoiceRepository.deleteAll()
        }

        // given
        val created = transactionTemplate.execute {
            invoiceRepository.save(
                Invoice(
                    invoiceNumber = "1",
                    orderNumber = "1",
                    total = BigDecimal("10.00"),
                    status = InvoiceStatus.CREATED,
                ),
            )
        }.get()

        // when
        val actual = invoiceRepository.getInvoiceByInvoiceNumber(created.invoiceNumber).get()

        // then
        assertThat(actual.invoiceNumber).isEqualTo(created.invoiceNumber)
    }
}
