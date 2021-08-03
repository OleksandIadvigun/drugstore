//package sigma.software.leovegas.drugstore.stock
//
//import org.assertj.core.api.Assertions
//import org.assertj.core.api.Assertions.assertThat
//import org.junit.jupiter.api.DisplayName
//import org.junit.jupiter.api.Test
//import org.junit.jupiter.api.fail
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.boot.test.context.SpringBootTest
//import org.springframework.transaction.support.TransactionTemplate
//import java.math.BigDecimal
//
//@DisplayName("ProductRepository test")
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//class StockRepositoryTest(
//    @Autowired val transactionalTemplate: TransactionTemplate,
//    @Autowired val stockRepository: StockRepository
//) {
//    @Test
//    fun `should find stocks views`() {
//
//        // given
//        val createStocksIds = listOf(
//            1L to 1,
//            2L to 10
//        ).map {
//            Stock(
//                productId = it.first,
//                quantity = it.second
//            )
//        }.map { s ->
//            transactionalTemplate.execute {
//                stockRepository.save(s)
//            }
//        }.map{
//            it?.id ?: fail("it may not be null")
//        }
//
//        // when
//        val views = stockRepository.findStockView(createStocksIds)
//
//        // then
//        assertThat(views).hasSize(2)
//
//        // and
//        assertThat(views[0].quantity).isEqualTo(1)
//        assertThat(views[1].quantity).isEqualTo(10)
//
//    }
//}
