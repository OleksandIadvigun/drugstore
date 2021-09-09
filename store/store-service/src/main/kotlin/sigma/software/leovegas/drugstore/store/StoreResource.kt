package sigma.software.leovegas.drugstore.store

import org.slf4j.Logger
import org.slf4j.LoggerFactory
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

    val logger: Logger = LoggerFactory.getLogger(StoreResource::class.java)

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/transfer-certificate")
    fun getTransferCertificates() = storeService.getTransferCertificates()

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/transfer-certificate/order/{orderNumber}")
    fun getTransferCertificateByOrderNumber(@PathVariable("orderNumber") orderNumber: Long) =
        storeService.getTransferCertificatesByOrderId(orderNumber)

    @PutMapping("/receive")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun receiveProducts(@RequestBody orderNumber: Long) = storeService.receiveProduct(orderNumber)

    @PutMapping("/deliver")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun deliverProducts(@RequestBody orderNumber: Long) = storeService.deliverProducts(orderNumber)

    @PutMapping("/availability")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun checkAvailability(@RequestBody products: List<DeliverProductsQuantityRequest>) =
        storeService.checkAvailability(products)

    @GetMapping("/check-transfer/{orderNumber}")
    @ResponseStatus(HttpStatus.OK)
    fun checkTransfer(@PathVariable orderNumber: Long) =
        storeService.checkTransfer(orderNumber)

    @ExceptionHandler(Throwable::class)
    fun handleNotFound(e: Throwable) = run {
        val status = when (e) {
            else -> HttpStatus.BAD_REQUEST
        }
        val error = ApiError(status.value(), status.reasonPhrase, e.message)
        logger.warn("$error , ${e.javaClass}")
        ResponseEntity.status(status).body(error)
    }
}

