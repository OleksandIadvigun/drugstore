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

public class OrderMapperTest {
    private static final Product PRODUCT = new Product(1L,"name1",null,BigDecimal.valueOf(1.0));
    private static final OrderDetails ORDER_DETAILS = new OrderDetails(1L,PRODUCT,2);
    private static final OrderDetailsDto ORDER_DETAILS_DTO = new OrderDetailsDto(
            1L,PRODUCT.getId(),PRODUCT.getName(),PRODUCT.getPrice(),2);
    private static final Order ORDER = new Order(1l, null, BigDecimal.valueOf(50.0));
    private static final OrderDto ORDER_DTO = new OrderDto(1L,null , BigDecimal.valueOf(50.0));

    private static final List<OrderDto> ORDER_DTO_LIST = new ArrayList<>();
    private static final List<Order> ORDER_LIST = new ArrayList<>();
    private static final List<OrderDetails> ORDER_DETAILS_LIST = new ArrayList<>();
    private static final List<OrderDetailsDto> ORDER_DETAILS_DTO_LIST = new ArrayList<>();


    @BeforeEach
    void setUp() {
        ORDER_LIST.add(ORDER);
        ORDER_DTO_LIST.add(ORDER_DTO);
        ORDER_DETAILS_LIST.add(ORDER_DETAILS);
        ORDER_DETAILS_DTO_LIST.add(ORDER_DETAILS_DTO);
        ORDER.setOrderDetailsList(ORDER_DETAILS_LIST);
        ORDER_DTO.setOrderDetailsDtoList(ORDER_DETAILS_DTO_LIST);


    }

    @Test
    void toRestDto() {
        OrderDto orderDtoActual = OrderMapper.toRestDto(ORDER);
        assertEquals(ORDER_DTO, orderDtoActual);
    }

    @Test
    void toEntity() {
        Order orderActual = OrderMapper.toEntity(ORDER_DTO);
        assertEquals(ORDER, orderActual);
    }

    @Test
    void toRestDtoList() {
        List<OrderDto> orderDtoListActual = OrderMapper.toRestDto(ORDER_LIST);
        assertEquals(ORDER_DTO_LIST, orderDtoListActual);
    }


}
