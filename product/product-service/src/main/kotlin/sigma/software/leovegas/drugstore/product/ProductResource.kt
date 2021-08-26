package sigma.software.leovegas.drugstore.product

import org.springframework.data.domain.Page
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
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import sigma.software.leovegas.drugstore.api.ApiError
import sigma.software.leovegas.drugstore.product.api.ProductRequest
import sigma.software.leovegas.drugstore.product.api.ProductResponse

@RestController
@RequestMapping("/api/v1/")
class ProductResource(private val service: ProductService) {

    @ResponseStatus(CREATED)
    @PostMapping("products")
    fun create(@RequestBody product: ProductRequest): ProductResponse = service.create(product)

    @ResponseStatus(OK)
    @GetMapping("products/{id}")
    fun getOne(@PathVariable id: Long): ProductResponse = service.getOne(id)

    @ResponseStatus(OK)
    @GetMapping("products")
    fun getProducts(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "5") size: Int,
        @RequestParam(defaultValue = "") search: String,
        @RequestParam(defaultValue = "default") sortField: String,
        @RequestParam(defaultValue = "DESC") sortDirection: String
    ): Page<ProductResponse> = service.getAll(page, size, search, sortField, sortDirection)

    @ResponseStatus(OK)
    @GetMapping("products-by-ids")
    fun getProductsByIds(@RequestParam("ids") ids: List<Long>): List<ProductResponse> {
        return service.getProductsByIds(ids)
    }

    @ResponseStatus(ACCEPTED)
    @PutMapping("products/{id}")
    fun update(@PathVariable id: Long, @RequestBody product: ProductRequest): ProductResponse =
        service.update(id, product)


    @ResponseStatus(NO_CONTENT)
    @DeleteMapping("products/{id}")
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
