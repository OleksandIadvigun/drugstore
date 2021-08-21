package sigma.software.leovegas.drugstore.accountancy.client

import feign.Headers
import feign.Param
import feign.RequestLine
import java.math.BigDecimal
import sigma.software.leovegas.drugstore.accountancy.api.PriceItemRequest
import sigma.software.leovegas.drugstore.accountancy.api.PriceItemResponse

@Headers("Content-Type: application/json")
interface AccountancyClient {

    @RequestLine("POST api/v1/accountancy/price-item")
    fun createPriceItem(request: PriceItemRequest): PriceItemResponse

    @RequestLine("PUT api/v1/accountancy/price-item/{id}")
    fun updatePriceItem(@Param id: Long, request: PriceItemRequest): PriceItemResponse

    @RequestLine("GET api/v1/accountancy/product-price")
    fun getProductsPrice(): Map<Long, BigDecimal>

    @RequestLine("GET api/v1/accountancy/product-price-by-ids/ids={ids}")
    fun getProductsPriceByIds(@Param ids: List<Long>): Map<Long, BigDecimal>
}
