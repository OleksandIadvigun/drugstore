package sigma.software.leovegas.drugstore.controller;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import sigma.software.leovegas.drugstore.dto.ProductDto;
import sigma.software.leovegas.drugstore.service.ProductServiceI;

@RestController
@RequestMapping("/products")
@AllArgsConstructor
public class ProductController {
    private final ProductServiceI service;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ProductDto> getProducts(){
        return service.getAll();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ProductDto getOne(@PathVariable("id") Long id){
        return service.getOne(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    private ProductDto add(@RequestBody ProductDto productDto){
        return service.create(productDto);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.CREATED)
    private ProductDto update(@PathVariable Long id, @RequestBody ProductDto productDto){
        return service.update(id, productDto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    private void update(@PathVariable Long id){
        service.delete(id);
    }
}
