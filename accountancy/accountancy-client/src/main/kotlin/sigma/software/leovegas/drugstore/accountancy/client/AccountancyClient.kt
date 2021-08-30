package sigma.software.leovegas.drugstore.accountancy.client

import feign.Headers
import feign.Param
import feign.RequestLine
import java.time.LocalDateTime
import sigma.software.leovegas.drugstore.accountancy.api.InvoiceRequest
import sigma.software.leovegas.drugstore.accountancy.api.InvoiceResponse
import sigma.software.leovegas.drugstore.accountancy.api.MarkupUpdateRequest
import sigma.software.leovegas.drugstore.accountancy.api.MarkupUpdateResponse
import sigma.software.leovegas.drugstore.accountancy.api.PriceItemRequest
import sigma.software.leovegas.drugstore.accountancy.api.PriceItemResponse
import sigma.software.leovegas.drugstore.accountancy.api.PurchasedCostsCreateRequest
import sigma.software.leovegas.drugstore.accountancy.api.PurchasedCostsResponse
import sigma.software.leovegas.drugstore.accountancy.api.PurchasedCostsUpdateRequest
import sigma.software.leovegas.drugstore.accountancy.api.PurchasedItemDTO

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

    @RequestLine("GET api/v1/accountancy/price-by-product-ids?ids={ids}&markup={markup}")
    fun getProductsPriceByProductIds(@Param ids: List<Long>, @Param markup: Boolean = true): List<PriceItemResponse>

    @RequestLine("GET api/v1/accountancy/price-items-by-ids?ids={ids}&markup={markup}")
    fun getPriceItemsByIds(@Param ids: List<Long>, @Param markup: Boolean = true): List<PriceItemResponse>

    @RequestLine("POST api/v1/accountancy/purchased-costs")
    fun createPurchasedCosts(request: PurchasedCostsCreateRequest): PurchasedCostsResponse

    @RequestLine("PUT api/v1/accountancy/purchased-costs/{id}")
    fun updatePurchasedCosts(@Param("id") id: Long, request: PurchasedCostsUpdateRequest): PurchasedCostsResponse

    @RequestLine("GET api/v1/accountancy/purchased-costs?dateFrom={dateFrom}&dateTo={dateTo}")
    fun getPurchasedCosts(
        @Param dateFrom: LocalDateTime? = null,
        @Param dateTo: LocalDateTime? = null
    ): List<PurchasedCostsResponse>

    @RequestLine("GET api/v1/accountancy/past-purchased-items")
    fun getPastPurchasedItems(): List<PurchasedItemDTO>
}
