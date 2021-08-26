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
import sigma.software.leovegas.drugstore.accountancy.api.PriceItemRequest
import sigma.software.leovegas.drugstore.accountancy.api.PriceItemResponse
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
    @PutMapping("/invoice/cancel/{id}")
    fun cancelInvoice(@PathVariable id: Long): InvoiceResponse =
        service.cancelInvoice(id)

    @ResponseStatus(OK)
    @GetMapping("/product-price")
    fun getProductsPrice(): Map<Long?, BigDecimal> = service.getProductsPrice()

    @ResponseStatus(OK)
    @GetMapping("/invoice/{id}")
    fun getInvoiceById(@PathVariable id: Long): InvoiceResponse = service.getInvoiceById(id)

    @ResponseStatus(OK)
    @GetMapping("/invoice/order-id/{id}")
    fun getInvoiceByOrderId(@PathVariable id: Long): InvoiceResponse = service.getInvoiceByOrderId(id)

    @ResponseStatus(OK)
    @GetMapping("/price-by-product-ids")
    fun getProductsPriceByProductIds(@RequestParam ids: List<Long>): Map<Long?, BigDecimal> =
        service.getProductsPriceByProductIds(ids)

    @ResponseStatus(OK)
    @GetMapping("/price-items-by-ids")
    fun getPriceItemsByIds(@RequestParam ids: List<Long>): List<PriceItemResponse> =
        service.getPriceItemsByIds(ids)

    @ExceptionHandler(Throwable::class)
    fun handleNotFound(e: Throwable) = run {
        val status = when (e) {
            is ResourceNotFoundException -> HttpStatus.BAD_REQUEST
            else -> HttpStatus.BAD_REQUEST
        }
        ResponseEntity.status(status).body(ApiError(status.value(), status.name, e.message))
    }
}
