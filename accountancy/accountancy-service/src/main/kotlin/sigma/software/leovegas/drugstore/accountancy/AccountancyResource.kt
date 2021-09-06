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
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import sigma.software.leovegas.drugstore.accountancy.api.CreateIncomeInvoiceRequest
import sigma.software.leovegas.drugstore.accountancy.api.CreateOutcomeInvoiceRequest
import sigma.software.leovegas.drugstore.accountancy.api.InvoiceResponse
import sigma.software.leovegas.drugstore.api.ApiError

@RestController
@RequestMapping("/api/v1/accountancy")
class AccountancyResource(private val service: AccountancyService) {

    @ResponseStatus(CREATED)
    @PostMapping("/invoice/income")
    fun createIncomeInvoice(@RequestBody productItems: CreateIncomeInvoiceRequest): InvoiceResponse =
        service.createIncomeInvoice(productItems)

    @ResponseStatus(CREATED)
    @PostMapping("/invoice/outcome")
    fun createOutcomeInvoice(@RequestBody createOutcomeInvoiceRequest: CreateOutcomeInvoiceRequest): InvoiceResponse =
        service.createOutcomeInvoice(createOutcomeInvoiceRequest)

    @ResponseStatus(ACCEPTED)
    @PutMapping("/invoice/pay/{id}")
    fun payInvoice(@PathVariable id: Long, @RequestBody money: BigDecimal): InvoiceResponse =
        service.payInvoice(id, money)

    @ResponseStatus(ACCEPTED)
    @PutMapping("/invoice/cancel/{id}")
    fun cancelInvoice(@PathVariable id: Long): InvoiceResponse =
        service.cancelInvoice(id)

    @ResponseStatus(ACCEPTED)
    @PutMapping("/invoice/refund/{id}")
    fun refundInvoice(@PathVariable id: Long): InvoiceResponse =
        service.refundInvoice(id)

    @ResponseStatus(OK)
    @GetMapping("/invoice/{id}")
    fun getInvoiceById(@PathVariable id: Long): InvoiceResponse = service.getInvoiceById(id)

    @ResponseStatus(OK)
    @GetMapping("/invoice/order-id/{id}")
    fun getInvoiceByOrderId(@PathVariable id: Long): InvoiceResponse = service.getInvoiceByOrderId(id)

//    @ResponseStatus(OK)
//    @GetMapping("/price-item/markup")
//    fun getMarkups(
//        @RequestParam(defaultValue = "") ids: List<Long>,
//    ): List<MarkupUpdateResponse> = service.getMarkUps(ids)
//
//    @ResponseStatus(ACCEPTED)
//    @PutMapping("/price-item/markup")
//    fun updateMarkup(@RequestBody markupUpdateRequests: List<MarkupUpdateRequest>): List<MarkupUpdateResponse> =
//        service.updateMarkups(markupUpdateRequests)

    @ExceptionHandler(Throwable::class)
    fun handleNotFound(e: Throwable) = run {
        val status = when (e) {
            is ResourceNotFoundException -> HttpStatus.BAD_REQUEST
            is InvalidStatusOfInvoice -> HttpStatus.BAD_REQUEST
            is ProductServiceResponseException -> HttpStatus.SERVICE_UNAVAILABLE
            else -> HttpStatus.BAD_REQUEST
        }
        ResponseEntity.status(status).body(ApiError(status.value(), status.name, e.message))
    }
}
