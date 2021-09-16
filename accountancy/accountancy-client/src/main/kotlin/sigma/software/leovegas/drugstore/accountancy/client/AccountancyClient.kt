package sigma.software.leovegas.drugstore.accountancy.client

import feign.Headers
import feign.Param
import feign.RequestLine
import java.math.BigDecimal
import sigma.software.leovegas.drugstore.accountancy.api.ConfirmOrderResponse
import sigma.software.leovegas.drugstore.accountancy.api.CreateIncomeInvoiceRequest
import sigma.software.leovegas.drugstore.accountancy.api.CreateOutcomeInvoiceEvent
import sigma.software.leovegas.drugstore.accountancy.api.ItemDTO

@Headers("Content-Type: application/json")
interface AccountancyClient {

    @RequestLine("POST api/v1/accountancy/invoice/income")
    fun createIncomeInvoice(request: CreateIncomeInvoiceRequest): ConfirmOrderResponse

    @RequestLine("POST /api/v1/accountancy/invoice/outcome")
    fun createOutcomeInvoice(request: CreateOutcomeInvoiceEvent): ConfirmOrderResponse

    @RequestLine("PUT /api/v1/accountancy/invoice/pay/{orderNumber}")
    fun payInvoice(@Param orderNumber: String, money: BigDecimal): ConfirmOrderResponse

    @RequestLine("PUT /api/v1/accountancy/invoice/cancel/{orderNumber}")
    fun cancelInvoice(@Param orderNumber: String): ConfirmOrderResponse

    @RequestLine("PUT /api/v1/accountancy/invoice/refund/{orderNumber}")
    fun refundInvoice(@Param orderNumber: String): ConfirmOrderResponse

    @RequestLine("GET /api/v1/accountancy/invoice/details/order-number/{orderNumber}")
    fun getInvoiceDetailsByOrderNumber(@Param orderNumber: String): List<ItemDTO>

    @RequestLine("GET /api/v1/accountancy/sale-price?productNumbers={productNumbers}")
    fun getSalePrice(@Param productNumbers: List<String>): Map<String, BigDecimal>
}
