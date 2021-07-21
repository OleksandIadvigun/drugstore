package sigma.software.leovegas.drugstore.dto;


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
public class OrderDto {

    private Long id;

    private List<OrderDetailsDto> orderDetailsDtoList;

    private BigDecimal total;

}
