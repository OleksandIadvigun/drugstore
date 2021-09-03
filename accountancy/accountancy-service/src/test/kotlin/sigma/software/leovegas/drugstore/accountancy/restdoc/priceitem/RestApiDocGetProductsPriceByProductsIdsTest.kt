//package sigma.software.leovegas.drugstore.accountancy.restdoc.priceitem
//
//import java.math.BigDecimal
//import org.hamcrest.Matchers.equalTo
//import org.junit.jupiter.api.DisplayName
//import org.junit.jupiter.api.Test
//import org.junit.jupiter.api.fail
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.boot.web.server.LocalServerPort
//import org.springframework.http.MediaType
//import org.springframework.transaction.support.TransactionTemplate
//import sigma.software.leovegas.drugstore.accountancy.client.AccountancyProperties
//import sigma.software.leovegas.drugstore.accountancy.restdoc.RestApiDocumentationTest
//
//@DisplayName("Get products price by products ids REST API Doc test")
//class RestApiDocGetProductsPriceByProductsIdsTest @Autowired constructor(
//    @LocalServerPort val port: Int,
//    val transactionTemplate: TransactionTemplate,
//    val accountancyProperties: AccountancyProperties,
//    val priceItemRepo: PriceItemRepository
//) : RestApiDocumentationTest(accountancyProperties) {
//
//
//    @Test
//    fun `should get products price by products ids`() {
//
//        //given
//        transactionTemplate.execute {
//            priceItemRepo.deleteAll()
//        }
//
//        // and
//        val saved = transactionTemplate.execute {
//            priceItemRepo.saveAll(
//                listOf(
//                    PriceItem(
//                        productId = 1L,
//                        price = BigDecimal("10.00"),
//                        markup = BigDecimal.ZERO
//                    ),
//                    PriceItem(
//                        productId = 2L,
//                        price = BigDecimal("10.00"),
//                        markup = BigDecimal.ZERO
//                    ),
//                    PriceItem(
//                        productId = 3L,
//                        price = BigDecimal("10.00"),
//                        markup = BigDecimal.ZERO
//                    )
//                )
//            )
//        } ?: fail("Fail, response is expected")
//
//        // then
//        of("get-products-price-by-products-ids").`when`()
//            .contentType(MediaType.APPLICATION_JSON_VALUE)
//            .get("http://${accountancyProperties.host}:$port/api/v1/accountancy/price-by-product-ids?ids=1,2")
//            .then()
//            .assertThat().statusCode(200)
//            .assertThat().body("size()", equalTo(2))
//    }
//}
