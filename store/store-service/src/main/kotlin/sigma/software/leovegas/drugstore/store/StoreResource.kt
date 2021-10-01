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
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import sigma.software.leovegas.drugstore.api.ApiError
import sigma.software.leovegas.drugstore.api.protobuf.Proto
import sigma.software.leovegas.drugstore.product.api.DeliverProductsQuantityRequest

@RestController
@RequestMapping("/api/v1/store")
class StoreResource(private val storeService: StoreService) {

    val logger: Logger = LoggerFactory.getLogger(StoreResource::class.java)

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/transfer-certificate")
    fun getTransferCertificates(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "5") size: Int,
    ) = storeService.getTransferCertificates(page, size)

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/transfer-certificate/order/{orderNumber}")
    fun getTransferCertificateByOrderNumber(@PathVariable("orderNumber") orderNumber: String) =
        storeService.getTransferCertificatesByOrderNumber(orderNumber)

    @PutMapping("/receive/{orderNumber}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun receiveProducts(@PathVariable("orderNumber") orderNumber: String) = storeService.receiveProduct(orderNumber)

    @PutMapping("/deliver/{orderNumber}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun deliverProducts(@PathVariable("orderNumber") orderNumber: String) = storeService.deliverProducts(orderNumber)

    @PutMapping("/availability")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun checkAvailability(@RequestBody products: List<DeliverProductsQuantityRequest>) =
        storeService.checkAvailability(products)

    @GetMapping("/check-transfer/{orderNumber}")
    @ResponseStatus(HttpStatus.OK)
    fun checkTransfer(@PathVariable orderNumber: String): Proto.CheckTransferResponse {
        return storeService.checkTransfer(orderNumber)
    }

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

