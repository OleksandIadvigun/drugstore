package sigma.software.leovegas.drugstore.accountancy.client

import feign.Headers
import feign.Param
import feign.RequestLine
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import sigma.software.leovegas.drugstore.accountancy.api.InvoiceRequest
import sigma.software.leovegas.drugstore.accountancy.api.InvoiceResponse
import sigma.software.leovegas.drugstore.accountancy.api.MarkupUpdateRequest
import sigma.software.leovegas.drugstore.accountancy.api.MarkupUpdateResponse
import sigma.software.leovegas.drugstore.accountancy.api.PriceItemRequest
import sigma.software.leovegas.drugstore.accountancy.api.PriceItemResponse
import sigma.software.leovegas.drugstore.accountancy.api.PurchasedCostsRequest
import sigma.software.leovegas.drugstore.accountancy.api.PurchasedCostsResponse

@Headers("Content-Type: application/json")
interface AccountancyClient {

    @RequestLine("POST api/v1/accountancy/price-item")
    fun createPriceItem(request: PriceItemRequest): PriceItemResponse

    @RequestLine("POST api/v1/accountancy/invoice")
    fun createInvoice(request: InvoiceRequest): InvoiceResponse

    @RequestLine("PUT api/v1/accountancy/price-item/{id}")
    fun updatePriceItem(@Param id: Long, request: PriceItemRequest): PriceItemResponse

    @RequestLine("PUT api/v1/accountancy/price-item/markup")
    fun updateMarkup(markupUpdateRequests: List<MarkupUpdateRequest>): List<MarkupUpdateResponse>

    @RequestLine("PUT api/v1/accountancy/invoice/cancel/{id}")
    fun cancelInvoice(@Param id: Long): InvoiceResponse

    @RequestLine("PUT api/v1/accountancy/invoice/pay/{id}")
    fun payInvoice(@Param id: Long): InvoiceResponse

    @RequestLine("PUT api/v1/accountancy/invoice/refund/{id}")
    fun refundInvoice(@Param id: Long): InvoiceResponse

    @RequestLine("GET api/v1/accountancy/product-price")
    fun getProductsPrice(): List<PriceItemResponse>

    @RequestLine("GET api/v1/accountancy/invoice/{id}")
    fun getInvoiceById(@Param id: Long): InvoiceResponse

    @RequestLine("GET api/v1/accountancy/invoice/order-id/{id}")
    fun getInvoiceByOrderId(@Param id: Long): InvoiceResponse

    @RequestLine("GET api/v1/accountancy/price-item/markup?ids={ids}")
    fun getMarkups(@Param ids: List<Long>): List<MarkupUpdateResponse>

    @RequestLine("GET api/v1/accountancy/price-by-product-ids/ids={ids}&markup={markup}")
    fun getProductsPriceByProductIds(@Param ids: List<Long>, @Param markup: Boolean = true): List<PriceItemResponse>

    @RequestLine("GET api/v1/accountancy/price-items-by-ids/ids={ids}&markup={markup}")
    fun getPriceItemsByIds(@Param ids: List<Long>, @Param markup: Boolean = true): List<PriceItemResponse>

    @RequestLine("POST api/v1/accountancy/purchased-costs")
    fun createPurchasedCosts(request: PurchasedCostsRequest): PurchasedCostsResponse
}
