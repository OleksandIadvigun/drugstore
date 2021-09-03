package sigma.software.leovegas.drugstore.store

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import sigma.software.leovegas.drugstore.api.ApiError
import sigma.software.leovegas.drugstore.product.api.DeliverProductsQuantityRequest

@RestController
@RequestMapping("/api/v1/store")
class StoreResource(private val storeService: StoreService) {

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/transfer-certificate")
    fun getTransferCertificates() = storeService.getTransferCertificates()

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/transfer-certificate/invoice/{id}")
    fun getTransferCertificateByInvoiceId(@PathVariable("id") id: Long) =
        storeService.getTransferCertificatesByInvoiceId(id)

    @PutMapping("/receive")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun receiveProducts(@RequestBody invoiceId: Long) = storeService.receiveProduct(invoiceId)

    @PutMapping("/return")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun returnProducts(@RequestBody invoiceId: Long) = storeService.returnProducts(invoiceId)

    @PutMapping("/deliver")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun deliverProducts(@RequestBody orderId: Long) = storeService.deliverProducts(orderId)

    @PutMapping("/availability")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun checkAvailability(@RequestBody products: List<DeliverProductsQuantityRequest>) =
        storeService.checkAvailability(products)

    @ExceptionHandler(Throwable::class)
    fun handleNotFound(e: Throwable) = run {
        val status = when (e) {
            is InsufficientAmountOfProductException -> HttpStatus.BAD_REQUEST
            is IncorrectTypeOfInvoice -> HttpStatus.BAD_REQUEST
            is IncorrectStatusOfInvoice -> HttpStatus.BAD_REQUEST
            is InvoiceNotPaidException -> HttpStatus.BAD_REQUEST
            else -> HttpStatus.BAD_REQUEST
        }
        ResponseEntity.status(status).body(ApiError(status.value(), status.name, e.message))
    }
}

