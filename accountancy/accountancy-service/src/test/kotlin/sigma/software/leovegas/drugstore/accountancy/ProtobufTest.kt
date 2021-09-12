//package sigma.software.leovegas.drugstore.accountancy
//
//import java.math.BigDecimal
//import org.assertj.core.api.Assertions.assertThat
//import org.junit.jupiter.api.BeforeEach
//import org.junit.jupiter.api.Disabled
//import org.junit.jupiter.api.Test
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.boot.test.context.SpringBootTest
//import org.springframework.boot.test.web.client.TestRestTemplate
//import org.springframework.boot.web.server.LocalServerPort
//import org.springframework.context.annotation.Import
//import org.springframework.http.HttpMethod
//import org.springframework.transaction.support.TransactionTemplate
//import sigma.software.leovegas.drugstore.accountancy.client.AccountancyProperties
//import sigma.software.leovegas.drugstore.api.protobuf.AccountancyProto
//import sigma.software.leovegas.drugstore.extensions.get
//
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@Import(RestBean::class)
//class ProtobufTest @Autowired constructor(
//    @LocalServerPort val port: Int,
//    val restTemplate: TestRestTemplate,
//    val accountancyProperties: AccountancyProperties,
//    val transactionalTemplate: TransactionTemplate,
//    val invoiceRepository: InvoiceRepository,
//) {
//
//    lateinit var baseUrl: String
//
//    @BeforeEach
//    fun setup() {
//        baseUrl = "http://${accountancyProperties.host}:$port"
//    }
//
//    @Disabled
//    @Test
//    fun `should get protobuf object`(){
//
//        // setup
//        transactionalTemplate.execute {
//            invoiceRepository.deleteAll()
//        }
//
//        // given
//        val savedInvoice = transactionalTemplate.execute {
//            invoiceRepository.save(
//                Invoice(
//                    invoiceNumber = "1",
//                    status = InvoiceStatus.PAID,
//                    type = InvoiceType.OUTCOME,
//                    orderNumber = "1",
//                    total = BigDecimal("90.00"),
//                    productItems = setOf(
//                        ProductItem(
//                            productNumber = "123",
//                            quantity = 3
//                        )
//                    )
//                )
//            )
//        }.get()
//
//    // when
//     val response = restTemplate.exchange("$baseUrl/api/v1/accountancy/invoice/details/order-number/${savedInvoice.orderNumber}",
//         HttpMethod.GET,null, AccountancyProto.InvoiceDetails::class.java)
//
//        assertThat(response.body?.getItems(0)?.productNumber).isEqualTo("123")
//    }
//}