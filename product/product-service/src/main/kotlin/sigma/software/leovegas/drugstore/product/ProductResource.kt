package sigma.software.leovegas.drugstore.product

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
import sigma.software.leovegas.drugstore.api.ApiError
import sigma.software.leovegas.drugstore.api.protobuf.Proto
import sigma.software.leovegas.drugstore.product.api.CreateProductsEvent
import sigma.software.leovegas.drugstore.product.api.GetProductResponse
import sigma.software.leovegas.drugstore.product.api.ProductDetailsResponse
import sigma.software.leovegas.drugstore.product.api.SearchProductResponse

@CrossOrigin
@RestController
@RequestMapping("/api/v1/products")
class ProductResource(private val service: ProductService) {

    val logger: Logger = LoggerFactory.getLogger(ProductResource::class.java)

    @ResponseStatus(OK)
    @GetMapping("/{productNumbers}/price")
    fun getProductPrice(@PathVariable("productNumbers") productNumbers: List<String>): Proto.ProductsPrice =
        service.getProductPrice(productNumbers)

    @ResponseStatus(CREATED)
    @PostMapping("")
    fun create(@RequestBody productRequest: CreateProductsEvent) = service.createProduct(productRequest)

    @ResponseStatus(OK)
    @GetMapping("/search")
    fun searchProducts(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "5") size: Int,
        @RequestParam(defaultValue = "") search: String,
        @RequestParam(defaultValue = "popularity") sortField: String,
        @RequestParam(defaultValue = "DESC") sortDirection: String
    ): List<SearchProductResponse> = service.searchProducts(page, size, search, sortField, sortDirection)

    @ResponseStatus(OK)
    @GetMapping("/popular")
    fun getPopularProducts(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "5") size: Int,
    ): List<GetProductResponse> = service.getPopularProducts(page, size)

    @ResponseStatus(ACCEPTED)
    @PutMapping("/deliver")
    fun deliverProducts(@RequestBody products: Proto.DeliverProductsDTO) =
        service.deliverProducts(products)

    @ResponseStatus(ACCEPTED)
    @PutMapping("/receive")
    fun receiveProducts(@RequestBody productNumbers: Proto.ProductNumberList) =
        service.receiveProducts(productNumbers)

    @ResponseStatus(OK)
    @GetMapping("/details")
    fun getProductsDetailsByIds(@RequestParam("productNumbers") productNumbers: List<String>)
            : Proto.ProductDetailsResponse {
        return service.getProductsDetailsByProductNumbers(productNumbers)
    }

    @ResponseStatus(OK)
    @GetMapping("/details/{productNumber}")
    fun getProductsDetailsByIdJson(@PathVariable("productNumber") productNumber: String)
            : ProductDetailsResponse {
        return service.getProductsDetailsByProductNumbersJson(productNumber)
    }

    @ExceptionHandler(Throwable::class)
    fun handleNotFound(e: Throwable) = run {
        val status = when (e) {
            is ResourceNotFoundException -> HttpStatus.BAD_REQUEST
            is NotCorrectRequestException -> HttpStatus.BAD_REQUEST
            is NotEnoughQuantityProductException -> HttpStatus.BAD_REQUEST
            is OrderServerException -> HttpStatus.GATEWAY_TIMEOUT
            else -> HttpStatus.BAD_REQUEST
        }
        val error = ApiError(status.value(), status.reasonPhrase, e.message)
        logger.warn("$error , ${e.javaClass}")
        ResponseEntity.status(status).body(error)
    }
}
