package sigma.software.leovegas.drugstore.accountancy.client

import feign.Headers
import feign.Param
import feign.RequestLine
import java.math.BigDecimal
import sigma.software.leovegas.drugstore.accountancy.api.InvoiceRequest
import sigma.software.leovegas.drugstore.accountancy.api.InvoiceResponse
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

    @RequestLine("PUT api/v1/accountancy/invoice/cancel/{id}")
    fun cancelInvoice(@Param id: Long): InvoiceResponse

    @RequestLine("GET api/v1/accountancy/product-price")
    fun getProductsPrice(): Map<Long, BigDecimal>

    @RequestLine("GET api/v1/accountancy/invoice/{id}")
    fun getInvoiceById(@Param id: Long): InvoiceResponse

    @RequestLine("GET api/v1/accountancy/invoice/order-id/{id}")
    fun getInvoiceByOrderId(@Param id: Long): InvoiceResponse

    @RequestLine("GET api/v1/accountancy/product-price-by-ids/ids={ids}")
    fun getProductsPriceByIds(@Param ids: List<Long>): Map<Long, BigDecimal>

    @RequestLine("GET api/v1/accountancy/price-by-product-ids/ids={ids}")
    fun getProductsPriceByProductIds(@Param ids: List<Long>): Map<Long, BigDecimal>

    @RequestLine("GET api/v1/accountancy/price-items-by-ids/ids={ids}")
    fun getPriceItemsByIds(@Param ids: List<Long>): List<PriceItemResponse>

    @RequestLine("POST api/v1/accountancy/purchased-costs")
    fun createPurchasedCosts(request: PurchasedCostsRequest): PurchasedCostsResponse
}
