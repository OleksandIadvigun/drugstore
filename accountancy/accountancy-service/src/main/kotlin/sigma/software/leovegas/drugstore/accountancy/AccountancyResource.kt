package sigma.software.leovegas.drugstore.accountancy

import java.math.BigDecimal
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.ACCEPTED
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.OK
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import sigma.software.leovegas.drugstore.accountancy.api.InvoiceRequest
import sigma.software.leovegas.drugstore.accountancy.api.InvoiceResponse
import sigma.software.leovegas.drugstore.accountancy.api.MarkupUpdateRequest
import sigma.software.leovegas.drugstore.accountancy.api.MarkupUpdateResponse
import sigma.software.leovegas.drugstore.accountancy.api.PriceItemRequest
import sigma.software.leovegas.drugstore.accountancy.api.PriceItemResponse
import sigma.software.leovegas.drugstore.accountancy.api.PurchasedCostsRequest
import sigma.software.leovegas.drugstore.accountancy.api.PurchasedCostsResponse
import sigma.software.leovegas.drugstore.api.ApiError

@RestController
@RequestMapping("/api/v1/accountancy")
class AccountancyResource(private val service: AccountancyService) {

    @ResponseStatus(CREATED)
    @PostMapping("/price-item")
    fun create(@RequestBody priceItemRequest: PriceItemRequest): PriceItemResponse =
        service.createPriceItem(priceItemRequest)

    @ResponseStatus(CREATED)
    @PostMapping("/invoice")
    fun createInvoice(@RequestBody invoiceRequest: InvoiceRequest): InvoiceResponse =
        service.createInvoice(invoiceRequest)

    @ResponseStatus(ACCEPTED)
    @PutMapping("/price-item/{id}")
    fun update(@PathVariable id: Long, @RequestBody priceItemRequest: PriceItemRequest): PriceItemResponse =
        service.updatePriceItem(id, priceItemRequest)

    @ResponseStatus(ACCEPTED)
    @PutMapping("/price-item/markup")
    fun updateMarkup(@RequestBody markupUpdateRequests: List<MarkupUpdateRequest>): List<MarkupUpdateResponse> =
        service.updateMarkups(markupUpdateRequests)

    @ResponseStatus(ACCEPTED)
    @PutMapping("/invoice/pay/{id}")
    fun payInvoice(@PathVariable id: Long): InvoiceResponse =
        service.payInvoice(id)

    @ResponseStatus(ACCEPTED)
    @PutMapping("/invoice/cancel/{id}")
    fun cancelInvoice(@PathVariable id: Long): InvoiceResponse =
        service.cancelInvoice(id)

    @ResponseStatus(ACCEPTED)
    @PutMapping("/invoice/refund/{id}")
    fun refundInvoice(@PathVariable id: Long): InvoiceResponse =
        service.refundInvoice(id)

    @ResponseStatus(OK)
    @GetMapping("/product-price")
    fun getProductsPrice(): List<PriceItemResponse> = service.getProductsPrice()

    @ResponseStatus(OK)
    @GetMapping("/invoice/{id}")
    fun getInvoiceById(@PathVariable id: Long): InvoiceResponse = service.getInvoiceById(id)

    @ResponseStatus(OK)
    @GetMapping("/invoice/order-id/{id}")
    fun getInvoiceByOrderId(@PathVariable id: Long): InvoiceResponse = service.getInvoiceByOrderId(id)

    @ResponseStatus(OK)
    @GetMapping("/price-by-product-ids")
    fun getProductsPriceByProductIds(
        @RequestParam ids: List<Long>,
        @RequestParam(defaultValue = "true") markup: Boolean
    ): List<PriceItemResponse> =
        service.getProductsPriceByProductIds(ids, markup)

    @ResponseStatus(OK)
    @GetMapping("/price-items-by-ids")
    fun getPriceItemsByIds(
        @RequestParam ids: List<Long>,
        @RequestParam(defaultValue = "true") markup: Boolean
    ): List<PriceItemResponse> =
        service.getPriceItemsByIds(ids, markup)

    @ResponseStatus(OK)
    @GetMapping("/price-item/markup")
    fun getMarkups(
        @RequestParam(defaultValue = "") ids: List<Long>,
    ): List<MarkupUpdateResponse> = service.getMarkUps(ids)

    @ResponseStatus(CREATED)
    @PostMapping("/purchased-costs")
    fun createPurchasedCosts(@RequestBody purchasedCostsRequest: PurchasedCostsRequest): PurchasedCostsResponse =
        service.createPurchasedCosts(purchasedCostsRequest)

    @ExceptionHandler(Throwable::class)
    fun handleNotFound(e: Throwable) = run {
        val status = when (e) {
            is ResourceNotFoundException -> HttpStatus.BAD_REQUEST
            is PriceItemNotFoundException -> HttpStatus.BAD_REQUEST
            is InvalidStatusOfInvoice -> HttpStatus.BAD_REQUEST
            else -> HttpStatus.BAD_REQUEST
        }
        ResponseEntity.status(status).body(ApiError(status.value(), status.name, e.message))
    }
}
