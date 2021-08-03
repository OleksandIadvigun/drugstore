//package sigma.software.leovegas.drugstore.stock
//
//import org.assertj.core.api.Assertions.assertThat
//import org.junit.jupiter.api.Test
//import org.junit.jupiter.api.assertThrows
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
//import org.springframework.boot.test.context.SpringBootTest
//import org.springframework.transaction.support.TransactionTemplate
//import sigma.software.leovegas.drugstore.product.Product
//import sigma.software.leovegas.drugstore.product.ProductRepository
//import java.math.BigDecimal
//import kotlin.test.assertTrue
//
//@SpringBootTest
//@AutoConfigureTestDatabase
//class StockServiceTest(
//    @Autowired val stockService: StockService,
//    @Autowired val productRepo: ProductRepository,
//    @Autowired val stockRepository: StockRepository,
//    @Autowired val transactionalTemplate: TransactionTemplate,
//) {
//
//    @Test
//    fun `should create stock`() {
//
//        // given
//        val savedProduct = transactionalTemplate.execute {
//            productRepo.save(
//                Product(
//                    name = "test",
//                    price = BigDecimal.TEN,
//                )
//            )
//        }
//
//        // when
//        val response = stockService.create(
//            StockRequest(
//                productId = savedProduct?.id,
//                quantity = 5
//            )
//        )
//
//        // then
//        assertThat(response.id).isNotNull
//        assertThat(response.productId).isEqualTo(savedProduct?.id)
//
//    }
//
//    @Test
//    fun `if product not exist should return ProductNotFoundException`() {
//        assertThrows<ProductIsNotExistException> {
//            stockService.create(
//                StockRequest(
//                    productId = Long.MAX_VALUE,
//                    quantity = 5
//                )
//            )
//        }
//    }
//
//    @Test
//    fun `if stock with product is already exist should return StockWithProductAlreadyExistException`() {
//
//        // given
//        val savedProduct = transactionalTemplate.execute {
//            productRepo.save(
//                Product(
//                    name = "test",
//                    price = BigDecimal.TEN,
//                )
//            )
//        }
//
//        // and
//        val savedStock = transactionalTemplate.execute {
//            stockRepository.save(
//                Stock(
//                    productId = savedProduct?.id,
//                    quantity = 5
//                )
//            )
//        }
//
//        // then
//        assertThat(savedStock?.id).isNotNull
//        assertThrows<StockWithThisProductAlreadyExistException> {
//            stockService.create(
//                StockRequest(
//                    productId = savedProduct?.id,
//                    quantity = 5
//                )
//            )
//        }
//    }
//
//    @Test
//    fun `should return stocks`() {
//
//        // when
//        val actual = stockService.getAll()
//
//        // then
//        assertThat(actual).isNotNull
//        assertTrue(actual::class.qualifiedName == "java.util.ArrayList")
//    }
//
//    @Test
//    fun `should update stock`() {
//        // given
//        val savedProduct = transactionalTemplate.execute {
//            productRepo.save(
//                Product(
//                    name = "test",
//                    price = BigDecimal.TEN,
//                )
//            )
//        }
//
//        // and
//        val stockRequest = stockService.create(
//            StockRequest(
//                productId = savedProduct?.id,
//                quantity = 5
//            )
//        )
//
//        // when
//        val updatedStock = stockService.update(
//            stockRequest.id!!,
//            StockRequest(
//                productId = savedProduct?.id,
//                quantity = 7
//            )
//        )
//
//        // then
//        assertThat(updatedStock.id).isNotNull
//        assertThat(updatedStock.quantity).isEqualTo(7)
//    }
//
//    @Test
//    fun `if stock is nox exist should return StockIsNotFoundException`() {
//        assertThrows<StockNotFoundException> {
//            stockService.update(
//                Long.MAX_VALUE,
//                StockRequest(
//                    productId = Long.MAX_VALUE,
//                    quantity = 5
//                )
//            )
//        }
//    }
//
//    @Test
//    fun `should delete stock`() {
//        // given
//        val savedProduct = transactionalTemplate.execute {
//            productRepo.save(
//                Product(
//                    name = "test",
//                    price = BigDecimal.TEN,
//                )
//            )
//        }
//
//        // and
//        val stockRequest = stockService.create(
//            StockRequest(
//                productId = savedProduct?.id,
//                quantity = 5
//            )
//        )
//
//        // when
//        stockService.delete(stockRequest.id!!)
//
//        // then
//        assertThrows<StockNotFoundException> {
//            stockService.getOne(savedProduct?.id!!)
//        }
//    }
//}
