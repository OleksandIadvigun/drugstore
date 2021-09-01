package sigma.software.leovegas.drugstore.product

import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.ACCEPTED
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.OK
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
import sigma.software.leovegas.drugstore.product.api.CreateProductRequest
import sigma.software.leovegas.drugstore.product.api.GetProductResponse
import sigma.software.leovegas.drugstore.product.api.ProductDetailsResponse
import sigma.software.leovegas.drugstore.product.api.ReduceProductQuantityRequest
import sigma.software.leovegas.drugstore.product.api.ReduceProductQuantityResponse
import sigma.software.leovegas.drugstore.product.api.SearchProductResponse

@RestController
@RequestMapping("/api/v1/")
class ProductResource(private val service: ProductService) {

    @ResponseStatus(CREATED)
    @PostMapping("products")
    fun create(@RequestBody productRequest: List<CreateProductRequest>) = service.createProduct(productRequest)

//    @ResponseStatus(OK)
//    @GetMapping("products/{id}")
//    fun getOne(@PathVariable id: Long): ProductResponse = service.getOne(id)

    @ResponseStatus(OK)
    @GetMapping("products/search")
    fun searchProducts(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "5") size: Int,
        @RequestParam(defaultValue = "") search: String,
        @RequestParam(defaultValue = "popularity") sortField: String,
        @RequestParam(defaultValue = "DESC") sortDirection: String
    ): Page<SearchProductResponse> = service.searchProducts(page, size, search, sortField, sortDirection)

    @ResponseStatus(OK)
    @GetMapping("products/popular")
    fun getPopularProducts(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "5") size: Int,
    ): Page<GetProductResponse> {
        return service.getPopularProducts(page, size)
    }

    @ResponseStatus(ACCEPTED)
    @PutMapping("products/reduce-quantity")
    fun reduceQuantity(@RequestBody products: List<ReduceProductQuantityRequest>): List<ReduceProductQuantityResponse> =
        service.reduceQuantity(products)

    @ResponseStatus(ACCEPTED)
    @PutMapping("products/receive")
    fun receiveProducts(@RequestBody ids: List<Long>) = service.receiveProducts(ids)

    @ResponseStatus(OK)
    @GetMapping("products/details")
    fun getProductsDetailsByIds(@RequestParam("ids") ids: List<Long>): List<ProductDetailsResponse> {
        return service.getProductsDetailsByIds(ids)
    }

    @ExceptionHandler(Throwable::class)
    fun handleNotFound(e: Throwable) = run {
        val status = when (e) {
            is ResourceNotFoundException -> HttpStatus.BAD_REQUEST
            is NotCorrectRequestException -> HttpStatus.BAD_REQUEST
            is NotEnoughQuantityProductException -> HttpStatus.BAD_REQUEST
            is InternalServerNotAvailableException -> HttpStatus.GATEWAY_TIMEOUT
            else -> HttpStatus.BAD_REQUEST
        }
        ResponseEntity.status(status).body(ApiError(status.value(), status.name, e.message))
    }
}
