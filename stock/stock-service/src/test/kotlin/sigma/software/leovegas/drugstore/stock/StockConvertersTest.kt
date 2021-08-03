//package sigma.software.leovegas.drugstore.stock
//
//import org.assertj.core.api.Assertions.assertThat
//import org.junit.jupiter.api.Test
//
//class StockConvertersTest {
//
//    @Test
//    fun `should convert to stock`(){
//
//        // given
//         val stock = Stock(productId = 12L, quantity = 5)
//         val stockRequest = StockRequest(productId = 12L, quantity = 5)
//
//        // when
//        val actual = stockRequest.convertToStock()
//
//        // then
//        assertThat(actual).isEqualTo(stock)
//    }
//
//    @Test
//    fun `should convert to stockResponse`(){
//
//        // given
//        val stock = Stock(productId = 12L, quantity = 5)
//        val stockResponse = StockResponse(productId = 12L, quantity = 5)
//
//        // when
//        val actual = stock.convertToStockResponse()
//
//        // then
//        assertThat(actual).isEqualTo(stockResponse)
//    }
//
//    @Test
//    fun `should convert to list of stockResponses`(){
//
//        // given
//        val stock = Stock(productId = 12L, quantity = 5)
//        val stockResponse = StockResponse(productId = 12L, quantity = 5)
//        val listOfStocks = mutableListOf<Stock>(stock, stock)
//        val expectedList = mutableListOf<StockResponse>(stockResponse, stockResponse)
//
//        // when
//        val actual = listOfStocks.convertToListOfStockResponses()
//
//        // then
//        assertThat(actual).isEqualTo(expectedList)
//    }
//}
