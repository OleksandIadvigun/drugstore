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
//import sigma.software.leovegas.drugstore.accountancy.AccountancyService
//import sigma.software.leovegas.drugstore.accountancy.api.MarkupUpdateRequest
//import sigma.software.leovegas.drugstore.accountancy.api.PriceItemRequest
//import sigma.software.leovegas.drugstore.accountancy.client.AccountancyProperties
//import sigma.software.leovegas.drugstore.accountancy.restdoc.RestApiDocumentationTest
//
//@DisplayName("Update markup in price item REST API Doc test")
//class RestApiDocUpdateMarkupInPriceItemTest @Autowired constructor(
//    val objectMapper: ObjectMapper,
//    @LocalServerPort val port: Int,
//    val transactionTemplate: TransactionTemplate,
//    val accountancyProperties: AccountancyProperties,
//    val accountancyService: AccountancyService
//) : RestApiDocumentationTest(accountancyProperties) {
//
//
//    @Test
//    fun `should update markup in price item`() {
//
//        // given
//        val priceItemCreated = transactionTemplate.execute {
//            accountancyService.createPriceItem(
//                PriceItemRequest(
//                    productId = 1L,
//                    price = BigDecimal("10.00"),
//                    markup = BigDecimal("0.20")
//                )
//            )
//        }
//
//        // and
//        val priceItemJson = objectMapper
//            .writerWithDefaultPrettyPrinter()
//            .writeValueAsString(
//                listOf(
//                    MarkupUpdateRequest(
//                        priceItemId = priceItemCreated?.id ?: -1,
//                        markup = BigDecimal("0.30")
//                    )
//                )
//            )
//
//        of("update-markup-in-price-item").`when`()
//            .body(priceItemJson)
//            .contentType(MediaType.APPLICATION_JSON_VALUE)
//            .put("http://${accountancyProperties.host}:$port/api/v1/accountancy/price-item/markup")
//            .then()
//            .assertThat().statusCode(202)
//            .assertThat().body("[0].priceItemId", equalTo((priceItemCreated?.id ?: -1).toInt()))
//            .assertThat().body("[0].price", equalTo(10.0F))
//            .assertThat().body("[0].markup", equalTo(0.30F))
//    }
//}
