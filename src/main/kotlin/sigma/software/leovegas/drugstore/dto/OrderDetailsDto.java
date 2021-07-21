package sigma.software.leovegas.drugstore.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetailsDto {

    @Schema(description = "id of the order details", example = "1",hidden = true)
    private Long id;

    @Schema(description = "id of the product", example = "1")
    private Long productId;

    @Schema(description = "id of the product", example = "paracetomol")
    private String name;

    @Schema(description = "price of the product", example = "10.5")
    private BigDecimal price;

    @Schema(description = "quaintity of the product to buy", example = "5")
    private Integer quantity;

}
