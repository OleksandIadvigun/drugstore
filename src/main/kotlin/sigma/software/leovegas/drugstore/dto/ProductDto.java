package sigma.software.leovegas.drugstore.dto;

import lombok.Builder;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.math.BigDecimal;

@Builder
@Data
public class ProductDto {

    private Long id;

    private String name;

    private Integer quantity;

    private BigDecimal price;

}
