package sigma.software.leovegas.drugstore.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Information about order")
public class OrderDto {

    @Schema(description = "id of the order", example = "1")
    private Long id;

    @Schema(description = "details of the order", implementation = OrderDetailsDto.class)
    private List<OrderDetailsDto> orderDetailsDtoList;

    @Schema(description = "total price of the order", example = "50.00")
    private BigDecimal total;

}
