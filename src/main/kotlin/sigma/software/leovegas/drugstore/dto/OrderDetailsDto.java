package sigma.software.leovegas.drugstore.dto;

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
    private Long id;

    private Long productId;

    private String name;

    private BigDecimal price;

    private Integer quantity;

}
