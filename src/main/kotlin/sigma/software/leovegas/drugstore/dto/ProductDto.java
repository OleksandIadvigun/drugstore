package sigma.software.leovegas.drugstore.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.DecimalMin;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Builder
@Data
public class ProductDto {
    @Schema(description = "id of the product", example = "1")
    private Long id;

    @Schema(description = "name of the product", example = "article")
    private String name;

    @Schema(description = "quantity of the products", example = "5")
    private Integer quantity;

    @Schema(description = "price of the product", example = "20.00")
    @DecimalMin(value = "0.00")
    private BigDecimal price;

}
