package sigma.software.leovegas.drugstore.accountancy


import java.math.BigDecimal
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.ACCEPTED
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.OK
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
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
import sigma.software.leovegas.drugstore.accountancy.api.ConfirmOrderResponse
import sigma.software.leovegas.drugstore.accountancy.api.CreateIncomeInvoiceRequest
import sigma.software.leovegas.drugstore.accountancy.api.CreateOutcomeInvoiceEvent
import sigma.software.leovegas.drugstore.accountancy.api.InvoiceResponse
import sigma.software.leovegas.drugstore.api.ApiError
import sigma.software.leovegas.drugstore.api.protobuf.Proto

@CrossOrigin
@RestController
@RequestMapping("/api/v1/accountancy")
class AccountancyResource(private val service: AccountancyService) {

    val logger: Logger = LoggerFactory.getLogger(AccountancyResource::class.java)

    @ResponseStatus(CREATED)
    @PostMapping("/invoice/income")
    fun createIncomeInvoice(@RequestBody createIncomeInvoiceRequest: CreateIncomeInvoiceRequest): ConfirmOrderResponse =
        service.createIncomeInvoice(createIncomeInvoiceRequest)

    @ResponseStatus(CREATED)
    @PostMapping("/invoice/outcome")
    fun createOutcomeInvoice(@RequestBody createOutcomeInvoiceEvent: CreateOutcomeInvoiceEvent): ConfirmOrderResponse =
        service.createOutcomeInvoice(createOutcomeInvoiceEvent)

    @ResponseStatus(ACCEPTED)
    @PutMapping("/invoice/pay/{orderNumber}")
    fun payInvoice(@PathVariable orderNumber: String, @RequestBody money: BigDecimal): ConfirmOrderResponse =
        service.payInvoice(orderNumber, money)

    @ResponseStatus(ACCEPTED)
    @PutMapping("/invoice/cancel/{orderNumber}")
    fun cancelInvoice(@PathVariable orderNumber: String): ConfirmOrderResponse =
        service.cancelInvoice(orderNumber)

    @ResponseStatus(ACCEPTED)
    @PutMapping("/invoice/refund/{orderNumber}")
    fun refundInvoice(@PathVariable orderNumber: String): ConfirmOrderResponse =
        service.refundInvoice(orderNumber)

    @ResponseStatus(OK)
    @GetMapping("/invoice/{invoiceNumber}")
    fun getInvoiceByInvoiceNumber(@PathVariable invoiceNumber: String): InvoiceResponse =
        service.getInvoiceByInvoiceNumber(invoiceNumber)

    @ResponseStatus(OK)
    @GetMapping("/sale-price")
    fun getSalePrice(@RequestParam("productNumbers") productNumbers: List<String>): Proto.ProductsPrice =
        service.getSalePrice(productNumbers)

    // protobuf request ----------------------------->

    @ResponseStatus(OK)
    @GetMapping("/invoice/details/order-number/{orderNumber}", produces = ["application/x-protobuf"])
    fun getInvoiceDetailsByOrderNumberProto(@PathVariable orderNumber: String): Proto.InvoiceDetails =
        service.getInvoiceDetailsByOrderNumber(orderNumber)

    @ExceptionHandler(Throwable::class)
    fun handleNotFound(e: Throwable) = run {
        val status = when (e) {
            is ProductServiceResponseException -> HttpStatus.SERVICE_UNAVAILABLE
            is OrderServiceResponseException -> HttpStatus.SERVICE_UNAVAILABLE
            is StoreServiceResponseException -> HttpStatus.SERVICE_UNAVAILABLE
            else -> HttpStatus.BAD_REQUEST
        }
        val error = ApiError(status.value(), status.reasonPhrase, e.message)
        logger.warn("$error , ${e.javaClass}")
        ResponseEntity.status(status).body(error)
    }
}
