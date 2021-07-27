package sigma.software.leovegas.drugstore.product

import org.springframework.http.HttpStatus.ACCEPTED
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.NO_CONTENT
import org.springframework.http.HttpStatus.OK
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/products")
class ProductResource(private val service: ProductService) {

    @PostMapping
    @ResponseStatus(CREATED)
    fun create(@RequestBody product: ProductRequest): ProductResponse {
        return service.create(product)
    }

    @GetMapping("/{id}")
    @ResponseStatus(OK)
    fun getOne(@PathVariable id: Long): ProductResponse {
        return service.getOne(id)
    }

    @GetMapping
    @ResponseStatus(OK)
    fun getProducts(): MutableList<ProductResponse> {
        return service.getAll()
    }

    @PutMapping("/{id}")
    @ResponseStatus(ACCEPTED)
    fun update(@PathVariable id: Long, @RequestBody product: ProductRequest): ProductResponse {
        return service.update(id, product)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(NO_CONTENT)
    fun delete(@PathVariable id: Long) {
        service.delete(id)
    }
}
