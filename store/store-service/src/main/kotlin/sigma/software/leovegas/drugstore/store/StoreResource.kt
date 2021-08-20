package sigma.software.leovegas.drugstore.store

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import sigma.software.leovegas.drugstore.api.ApiError
import sigma.software.leovegas.drugstore.store.api.CreateStoreRequest
import sigma.software.leovegas.drugstore.store.api.UpdateStoreRequest


@RestController
@RequestMapping("/api/v1/store")
class StoreResource(private val storeService: StoreService) {

    @PostMapping(path = ["", "/"])
    @ResponseStatus(HttpStatus.CREATED)
    fun createStoreItem(@RequestBody createStoreRequest: CreateStoreRequest) =
        storeService.createStoreItem(createStoreRequest)

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = ["", "/"])
    fun getStoreItems() = storeService.getStoreItems()

    @GetMapping("/price-ids")
    @ResponseStatus(HttpStatus.OK)
    fun getStoreItemsByPriceItemsId(@RequestParam("ids") ids: List<Long>) =
        storeService.getStoreItemsByPriceItemsId(ids)

    @PutMapping("/increase")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun increaseQuantity(@RequestBody requests: List<UpdateStoreRequest>) = storeService.increaseQuantity(requests)

    @PutMapping("/reduce")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun reduceQuantity(@RequestBody requests: List<UpdateStoreRequest>) = storeService.reduceQuantity(requests)

    @PutMapping("/check")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun checkAvailability(@RequestBody requests: List<UpdateStoreRequest>) = storeService.checkAvailability(requests)

    @ExceptionHandler(Throwable::class)
    fun handleNotFound(e: Throwable) = run {
        val status = when (e) {
            is StoreItemWithThisPriceItemAlreadyExistException -> HttpStatus.BAD_REQUEST
            is InsufficientAmountOfStoreItemException -> HttpStatus.BAD_REQUEST
            else -> HttpStatus.BAD_REQUEST
        }
        ResponseEntity.status(status).body(ApiError(status.value(), status.name, e.message))
    }
}

