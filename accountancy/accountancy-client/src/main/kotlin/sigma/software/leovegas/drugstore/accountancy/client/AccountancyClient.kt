package sigma.software.leovegas.drugstore.accountancy.client

import feign.Headers
import feign.Param
import feign.RequestLine
import sigma.software.leovegas.drugstore.accountancy.api.CreateIncomeInvoiceRequest
import sigma.software.leovegas.drugstore.accountancy.api.CreateOutcomeInvoiceRequest
import sigma.software.leovegas.drugstore.accountancy.api.InvoiceResponse

@Headers("Content-Type: application/json")
interface AccountancyClient {

    @RequestLine("PUT api/v1/accountancy/invoice/deliver/{id}")
    fun deliverByInvoice(@Param id: Long): InvoiceResponse

    @RequestLine("PUT api/v1/accountancy/invoice/receive/{id}")
    fun receiveByInvoice(@Param id: Long): InvoiceResponse

    @RequestLine("POST api/v1/accountancy/invoice/income")
    fun createIncomeInvoice(request: CreateIncomeInvoiceRequest): InvoiceResponse

    @RequestLine("POST api/v1/accountancy/invoice/outcome")
    fun createOutcomeInvoice(request: CreateOutcomeInvoiceRequest): InvoiceResponse

    @RequestLine("PUT api/v1/accountancy/invoice/pay/{id}")
    fun payInvoice(@Param id: Long): InvoiceResponse

    @RequestLine("PUT api/v1/accountancy/invoice/cancel/{id}")
    fun cancelInvoice(@Param id: Long): InvoiceResponse

    @RequestLine("PUT api/v1/accountancy/invoice/refund/{id}")
    fun refundInvoice(@Param id: Long): InvoiceResponse

    @RequestLine("GET api/v1/accountancy/invoice/{id}")
    fun getInvoiceById(@Param id: Long): InvoiceResponse

    @RequestLine("GET api/v1/accountancy/invoice/order-id/{id}")
    fun getInvoiceByOrderId(@Param id: Long): InvoiceResponse

//    @RequestLine("PUT api/v1/accountancy/price-item/markup")
//    fun updateMarkup(markupUpdateRequests: List<MarkupUpdateRequest>): List<MarkupUpdateResponse>
//
//    @RequestLine("GET api/v1/accountancy/price-item/markup?ids={ids}")
//    fun getMarkups(@Param ids: List<Long>): List<MarkupUpdateResponse>

}
