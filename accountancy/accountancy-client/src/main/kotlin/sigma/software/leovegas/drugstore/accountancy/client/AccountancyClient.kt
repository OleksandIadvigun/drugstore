package sigma.software.leovegas.drugstore.accountancy.client

import feign.Headers
import feign.Param
import feign.RequestLine
import java.math.BigDecimal
import sigma.software.leovegas.drugstore.accountancy.api.ConfirmOrderResponse
import sigma.software.leovegas.drugstore.accountancy.api.CreateIncomeInvoiceRequest
import sigma.software.leovegas.drugstore.accountancy.api.CreateOutcomeInvoiceRequest
import sigma.software.leovegas.drugstore.accountancy.api.ItemDTO

@Headers("Content-Type: application/json")
interface AccountancyClient {

    @RequestLine("POST api/v1/accountancy/invoice/income")
    fun createIncomeInvoice(request: CreateIncomeInvoiceRequest): ConfirmOrderResponse

    @RequestLine("POST /api/v1/accountancy/invoice/outcome")
    fun createOutcomeInvoice(request: CreateOutcomeInvoiceRequest): ConfirmOrderResponse

    @RequestLine("PUT /api/v1/accountancy/invoice/pay/{id}")
    fun payInvoice(@Param id: Long): ConfirmOrderResponse

    @RequestLine("PUT /api/v1/accountancy/invoice/cancel/{id}")
    fun cancelInvoice(@Param id: Long): ConfirmOrderResponse

    @RequestLine("PUT /api/v1/accountancy/invoice/refund/{id}")
    fun refundInvoice(@Param id: Long): ConfirmOrderResponse

    @RequestLine("GET /api/v1/accountancy/invoice/details/order-id/{orderId}")
    fun getInvoiceDetailsByOrderId(@Param orderId: Long): List<ItemDTO>

    @RequestLine("GET /api/v1/accountancy/sale-price?ids={ids}")
    fun getSalePrice(@Param ids: List<Long>): Map<Long, BigDecimal>
}
