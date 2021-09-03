//package sigma.software.leovegas.drugstore.accountancy.restdoc.priceitem
//
//import com.fasterxml.jackson.databind.ObjectMapper
//import java.math.BigDecimal
//import org.hamcrest.Matchers.equalTo
//import org.junit.jupiter.api.DisplayName
//import org.junit.jupiter.api.Test
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.boot.web.server.LocalServerPort
//import org.springframework.http.MediaType
//import org.springframework.transaction.support.TransactionTemplate
//import sigma.software.leovegas.drugstore.accountancy.client.AccountancyProperties
//import sigma.software.leovegas.drugstore.accountancy.restdoc.RestApiDocumentationTest
//
//@DisplayName("Get markups by price items ids REST API Doc test")
//class RestApiDocGetMarkupsByPriceItemIdsTest @Autowired constructor(
//    val objectMapper: ObjectMapper,
//    @LocalServerPort val port: Int,
//    val transactionTemplate: TransactionTemplate,
//    val accountancyProperties: AccountancyProperties,
//    val priceItemRepo: PriceItemRepository
//) : RestApiDocumentationTest(accountancyProperties) {
//
//    @Test
//    fun `should get markups by price items ids `() {
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
//                        markup = BigDecimal("0.20")
//                    ),
//                    PriceItem(
//                        productId = 2L,
//                        price = BigDecimal("10.00"),
//                        markup = BigDecimal("0.20")
//                    )
//                )
//            )
//        }
//
//        // given
//
//        of("get-markups-by-price-item-ids").`when`()
//            .contentType(MediaType.APPLICATION_JSON_VALUE)
//            .get("http://${accountancyProperties.host}:$port/api/v1/accountancy/price-item/markup?ids=${saved?.get(0)?.id}")
//            .then()
//            .assertThat().statusCode(200)
//            .assertThat().body("size()", equalTo(1))
//            .assertThat().body("[0].markup", equalTo(0.20F))
//    }
//}
