package sigma.software.leovegas.drugstore.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import sigma.software.leovegas.drugstore.dto.ProductDto;
import sigma.software.leovegas.drugstore.service.ProductServiceI;

@RestController
@RequestMapping("/products")
@AllArgsConstructor
@Tag(name = "Product Controller", description = "Allows to create/cancel/update products")
public class ProductController {
    private final ProductServiceI service;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get all products", description = "Allows to get all products")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Found products",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "The page is not found",
                    content = @Content(mediaType = "application/json"))
    })
    public List<ProductDto> getProducts() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get one product", description = "Allows to get one product")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Found product",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductDto.class))),
            @ApiResponse(responseCode = "404", description = "Product is not exist!",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(mediaType = "application/json"))
    })
    public ProductDto getOne(@PathVariable("id")
                             @Parameter(
                                     description = "The id of the product",
                                     content = @Content(schema = @Schema(implementation = Long.class)))
                                     Long id) {
        return service.getOne(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create product", description = "Allows to create product")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "order created",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(mediaType = "application/json"))
    })
    private ProductDto add(@RequestBody
                           @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Input name, quantity and price",
                                   content = @Content(schema = @Schema(implementation = ProductDto.class)))
                                   ProductDto productDto) {
        return service.create(productDto);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Update product", description = "Allows to update product")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Updated product",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductDto.class))),
            @ApiResponse(responseCode = "404", description = "Product is not exist!",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(mediaType = "application/json"))
    })
    private ProductDto update(@PathVariable
                              @Parameter(
                                      description = "The id of the product",
                                      content = @Content(schema = @Schema(implementation = Long.class)))
                                      Long id, @RequestBody
                              @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Input all values",
                                      content = @Content(schema = @Schema(implementation = ProductDto.class)))
                                      ProductDto productDto) {
        return service.update(id, productDto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Allows to cancel the product",
            responses = {
                    @ApiResponse(responseCode = "204", description = "The product has been successfully cancelled"),
                    @ApiResponse(responseCode = "403", description = "The product was fulfilled or cancelled"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "404", description = "The proposed product's id can not be found")
            })
    private void update(@PathVariable
                        @Parameter(
                                description = "The id of the product",
                                content = @Content(schema = @Schema(implementation = Long.class)))
                                Long id) {
        service.delete(id);
    }
}
