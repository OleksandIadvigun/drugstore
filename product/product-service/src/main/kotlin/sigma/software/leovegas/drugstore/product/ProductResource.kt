package sigma.software.leovegas.drugstore.product

import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.ACCEPTED
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.NO_CONTENT
import org.springframework.http.HttpStatus.OK
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import sigma.software.leovegas.drugstore.api.ApiError
import sigma.software.leovegas.drugstore.product.api.ProductRequest
import sigma.software.leovegas.drugstore.product.api.ProductResponse

@RestController
@RequestMapping("/api/v1/products")
class ProductResource(private val service: ProductService) {

    @ResponseStatus(CREATED)
    @PostMapping(path = ["", "/"])
    fun create(@RequestBody product: ProductRequest): ProductResponse = service.create(product)

    @ResponseStatus(OK)
    @GetMapping("/{id}")
    fun getOne(@PathVariable id: Long): ProductResponse = service.getOne(id)

    @ResponseStatus(OK)
    @GetMapping(path = ["", "/"])
    fun getProducts(): List<ProductResponse> = service.getAll()

    @PutMapping("/{id}")
    @ResponseStatus(ACCEPTED)
    fun update(@PathVariable id: Long, @RequestBody product: ProductRequest): ProductResponse =
        service.update(id, product)

    @DeleteMapping("/{id}")
    @ResponseStatus(NO_CONTENT)
    fun delete(@PathVariable id: Long) = service.delete(id)

    @ExceptionHandler(Throwable::class)
    fun handleNotFound(e: Throwable) = run {
        val status = when (e) {
            is ResourceNotFoundException -> HttpStatus.BAD_REQUEST
            else -> HttpStatus.BAD_REQUEST
        }
        ResponseEntity.status(status).body(ApiError(status.value(), status.name, e.message))
    }
}
