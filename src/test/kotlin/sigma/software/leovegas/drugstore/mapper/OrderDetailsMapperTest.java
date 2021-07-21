package sigma.software.leovegas.drugstore.mapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sigma.software.leovegas.drugstore.dto.OrderDetailsDto;
import sigma.software.leovegas.drugstore.dto.OrderDto;
import sigma.software.leovegas.drugstore.persistence.entity.Order;
import sigma.software.leovegas.drugstore.persistence.entity.OrderDetails;
import sigma.software.leovegas.drugstore.persistence.entity.Product;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OrderDetailsMapperTest {
    private static final Product PRODUCT = new Product(1L,"name1",null,BigDecimal.valueOf(1.0));
    private static final OrderDetails ORDER_DETAILS = new OrderDetails(1L,PRODUCT,2);
    private static final OrderDetailsDto ORDER_DETAILS_DTO = new OrderDetailsDto(
            1L,PRODUCT.getId(),PRODUCT.getName(),PRODUCT.getPrice(),2);

    private static final List<OrderDetails> ORDER_DETAILS_LIST = new ArrayList<>();
    private static final List<OrderDetailsDto> ORDER_DETAILS_DTO_LIST = new ArrayList<>();


    @BeforeEach
    void setUp() {
        ORDER_DETAILS_LIST.add(ORDER_DETAILS);
        ORDER_DETAILS_DTO_LIST.add(ORDER_DETAILS_DTO);



    }

    @Test
    void toRestDto() {
        OrderDetailsDto orderDetailsDtoActual = OrderDetailsMapper.toRestDto(ORDER_DETAILS);
        assertEquals(ORDER_DETAILS_DTO, orderDetailsDtoActual);
    }

    @Test
    void toEntity() {
        OrderDetails orderDetailsActual = OrderDetailsMapper.toEntity(ORDER_DETAILS_DTO);
        assertEquals(ORDER_DETAILS, orderDetailsActual);
    }

    @Test
    void toRestDtoList() {
        List<OrderDetailsDto> orderDetailsDtoListActual = OrderDetailsMapper.toRestDto(ORDER_DETAILS_LIST);
        assertEquals(ORDER_DETAILS_DTO_LIST, orderDetailsDtoListActual);
    }

    @Test
    void toEntityList() {
        List<OrderDetails> orderDetailsListActual = OrderDetailsMapper.toEntity(ORDER_DETAILS_DTO_LIST);
        assertEquals(ORDER_DETAILS_LIST, orderDetailsListActual);
    }


}
