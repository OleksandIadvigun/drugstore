//package sigma.software.leovegas.drugstore.accountancy
//
//import java.time.LocalDateTime
//import org.assertj.core.api.Assertions.assertThat
//import org.junit.jupiter.api.DisplayName
//import org.junit.jupiter.api.Test
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.boot.test.context.SpringBootTest
//import org.springframework.transaction.support.TransactionTemplate
//
//@DisplayName("Purchase Costs Repository test")
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//class PurchaseCostsRepositoryTest @Autowired constructor(
//    val transactionTemplate: TransactionTemplate,
//    val purchasedCostsRepository: PurchasedCostsRepository
//) {
//
//    @Test
//    fun `should get purchased costs before certain date`() {
//
//        // given
//        transactionTemplate.execute {
//            purchasedCostsRepository.deleteAll()
//        }
//
//        // and
//        transactionTemplate.execute {
//            purchasedCostsRepository.saveAll(
//                listOf(
//                    PurchasedCosts(
//                        priceItemId = 1,
//                        quantity = 5,
//                    ),
//                    PurchasedCosts(
//                        priceItemId = 1,
//                        quantity = 5,
//                    ),
//                )
//            )
//        }
//
//        // when
//        val actual = purchasedCostsRepository.findAllByDateOfPurchaseIsBefore(LocalDateTime.now().plusDays(1))
//
//        // then
//        assertThat(actual).hasSize(2)
//        assertThat(actual[0].dateOfPurchase).isBefore(LocalDateTime.now().plusDays(1))
//        assertThat(actual[1].dateOfPurchase).isBefore(LocalDateTime.now().plusDays(1))
//    }
//
//    @Test
//    fun `should get purchased costs after certain date`() {
//
//        // given
//        transactionTemplate.execute {
//            purchasedCostsRepository.deleteAll()
//        }
//
//        // and
//        transactionTemplate.execute {
//            purchasedCostsRepository.saveAll(
//                listOf(
//                    PurchasedCosts(
//                        priceItemId = 1,
//                        quantity = 5,
//                    ),
//                    PurchasedCosts(
//                        priceItemId = 1,
//                        quantity = 5,
//                    ),
//                )
//            )
//        }
//
//        // when
//        val actual = purchasedCostsRepository.findAllByDateOfPurchaseIsAfter(LocalDateTime.now().minusDays(1))
//
//        // then
//        assertThat(actual).hasSize(2)
//        assertThat(actual[0].dateOfPurchase).isAfter(LocalDateTime.now().minusDays(1))
//        assertThat(actual[1].dateOfPurchase).isAfter(LocalDateTime.now().minusDays(1))
//    }
//
//    @Test
//    fun `should get purchased between certain date`() {
//
//        // given
//        transactionTemplate.execute {
//            purchasedCostsRepository.deleteAll()
//        }
//
//        // and
//        transactionTemplate.execute {
//            purchasedCostsRepository.saveAll(
//                listOf(
//                    PurchasedCosts(
//                        priceItemId = 1,
//                        quantity = 5,
//                    ),
//                    PurchasedCosts(
//                        priceItemId = 1,
//                        quantity = 5,
//                    ),
//                )
//            )
//        }
//
//        // when
//        val notFoundPurchasedCosts = purchasedCostsRepository.findAllByDateOfPurchaseIsBetween(
//            LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2)
//        )
//        val foundPurchasedCosts = purchasedCostsRepository.findAllByDateOfPurchaseIsBetween(
//            LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(2)
//        )
//
//        // then
//        assertThat(notFoundPurchasedCosts).hasSize(0)
//        assertThat(foundPurchasedCosts).hasSize(2)
//        assertThat(foundPurchasedCosts[0].dateOfPurchase)
//            .isBetween(LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(2))
//        assertThat(foundPurchasedCosts[1].dateOfPurchase)
//            .isBetween(LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(2))
//    }
//}
