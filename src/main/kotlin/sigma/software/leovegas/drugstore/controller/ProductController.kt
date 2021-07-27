package sigma.software.leovegas.drugstore.controller;

import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sigma.software.leovegas.drugstore.dto.ProductRequest
import sigma.software.leovegas.drugstore.dto.ProductResponse
import sigma.software.leovegas.drugstore.service.ProductServiceI

@RestController
@RequestMapping("/products")
class ProductController(val service: ProductServiceI) {

    @GetMapping
    fun getAll(): MutableList<ProductResponse> {
        return service.getAll()
    }

    @GetMapping("/{id}")
    fun getOne(@PathVariable id: Long): ProductResponse {
        return service.getOne(id)
    }

    @PostMapping
    fun save(@RequestBody product: ProductRequest): ProductResponse {
        return service.save(product)
    }

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody product: ProductRequest): ProductResponse {
        return service.update(id, product)
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long){
        service.delete(id)
    }
}
