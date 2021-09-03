//package sigma.software.leovegas.drugstore.accountancy
//
//import java.math.BigDecimal
//import java.time.LocalDateTime
//import org.assertj.core.api.Assertions.assertThat
//import org.junit.jupiter.api.DisplayName
//import org.junit.jupiter.api.Test
//import org.junit.jupiter.api.fail
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.boot.test.context.SpringBootTest
//import org.springframework.transaction.support.TransactionTemplate
//
//@DisplayName("Invoice Repository test")
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//class InvoiceRepositoryTest @Autowired constructor(
//    val transactionTemplate: TransactionTemplate,
//    val invoiceRepository: InvoiceRepository
//) {
//
//    @Test
//    fun `should get invoice by order id`() {
//
//        // given
//        transactionTemplate.execute {
//            invoiceRepository.deleteAll()
//        }
//
//        // and
//        val created = transactionTemplate.execute {
//            invoiceRepository.save(
//                Invoice(
//                    orderId = 1,
//                    total = BigDecimal("10.00"),
//                    status = InvoiceStatus.CREATED,
//                )
//            )
//        } ?: fail("result is expected")
//
//        // when
//        val actual = invoiceRepository.getInvoiceByOrderId(created.orderId ?: -1).get()
//
//        // then
//        assertThat(actual.id).isEqualTo(created.id)
//        assertThat(actual.total).isEqualTo(created.total)
//        assertThat(actual.status).isEqualTo(created.status)
//
//    }
//
//    @Test
//    fun `should get invoice by status and createdAt less than`() {
//
//        // given
//        transactionTemplate.execute {
//            invoiceRepository.deleteAll()
//        }
//
//        // and
//        val created = transactionTemplate.execute {
//            invoiceRepository.saveAll(
//                listOf(
//                    Invoice(
//                        orderId = 1,
//                        total = BigDecimal("10.00"),
//                        status = InvoiceStatus.CREATED,
//                    ),
//                    Invoice(
//                        orderId = 2,
//                        total = BigDecimal("10.00"),
//                        status = InvoiceStatus.PAID,
//                    )
//                )
//            )
//        } ?: fail("result is expected")
//
//        // when
//        val actual = invoiceRepository.findAllByStatusAndCreatedAtLessThan(
//            InvoiceStatus.CREATED,
//            LocalDateTime.now().plusDays(1)
//        )
//
//        // then
//        assertThat(actual).hasSize(1)
//        assertThat(actual[0].id).isEqualTo(created[0].id)
//        assertThat(actual[0].orderId).isEqualTo(1)
//        assertThat(actual[0].total).isEqualTo(BigDecimal("10.00"))
//        assertThat(actual[0].status).isEqualTo(InvoiceStatus.CREATED)
//    }
//
//    @Test
//    fun `should get invoice by status`() {
//
//        // given
//        transactionTemplate.execute {
//            invoiceRepository.deleteAll()
//        }
//
//        // and
//        val created = transactionTemplate.execute {
//            invoiceRepository.saveAll(
//                listOf(
//                    Invoice(
//                        orderId = 1,
//                        total = BigDecimal("10.00"),
//                        status = InvoiceStatus.CREATED,
//                    ),
//                    Invoice(
//                        orderId = 2,
//                        total = BigDecimal("10.00"),
//                        status = InvoiceStatus.PAID,
//                    )
//                )
//            )
//        } ?: fail("result is expected")
//
//        // when
//        val actual = invoiceRepository.findAllByStatus(InvoiceStatus.CREATED)
//
//        // then
//        assertThat(actual).hasSize(1)
//        assertThat(actual[0].id).isEqualTo(created[0].id)
//        assertThat(actual[0].orderId).isEqualTo(1)
//        assertThat(actual[0].total).isEqualTo(BigDecimal("10.00"))
//        assertThat(actual[0].status).isEqualTo(InvoiceStatus.CREATED)
//    }
//
//    @Test
//    fun `should get purchased items `() {
//
//        // given
//        transactionTemplate.execute {
//            invoiceRepository.deleteAll()
//        }
//
//        // and
//        val createdToCalc = transactionTemplate.execute {
//            invoiceRepository.saveAll(
//                listOf(
//                    Invoice(
//                        orderId = 1,
//                        total = BigDecimal("50.00"),
//                        status = InvoiceStatus.CREATED,
//                        productItems = setOf(
//                            ProductItem(
//                                priceItemId = 1,
//                                name = "test1",
//                                price = BigDecimal.TEN,
//                                quantity = 5
//                            )
//                        )
//                    ),
//                    Invoice(
//                        orderId = 2,
//                        total = BigDecimal("50.00"),
//                        status = InvoiceStatus.PAID,
//                        productItems = setOf(
//                            ProductItem(
//                                priceItemId = 1,
//                                name = "test1",
//                                price = BigDecimal.TEN,
//                                quantity = 5
//                            )
//                        )
//                    ),
//                    Invoice(
//                        orderId = 3,
//                        total = BigDecimal("20.00"),
//                        status = InvoiceStatus.PAID,
//                        productItems = setOf(
//                            ProductItem(
//                                priceItemId = 2,
//                                name = "test1",
//                                price = BigDecimal.TEN,
//                                quantity = 2
//                            )
//                        )
//                    ),
//                )
//            )
//        } ?: fail("result is expected")
//
//        val createdToOmit = transactionTemplate.execute {
//            invoiceRepository.save(
//                Invoice(
//                    orderId = 4,
//                    total = BigDecimal("20.00"),
//                    status = InvoiceStatus.CREATED,
//                    productItems = setOf(
//                        ProductItem(
//                            priceItemId = 2,
//                            name = "test1",
//                            price = BigDecimal.TEN,
//                            quantity = 2
//                        )
//                    )
//                )
//            )
//        }
//
//        // and
//        val ids = createdToCalc.map {
//            it.productItems.map { item -> item.id ?: -1 }
//        }.flatten()
//
//        // when
//        val actual = invoiceRepository.getPurchasedItems(ids)
//
//        // then
//        assertThat(actual[0].quantity).isEqualTo(10)
//        assertThat(actual[1].quantity).isEqualTo(2)
//    }
//}
