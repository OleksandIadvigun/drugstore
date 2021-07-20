package sigma.software.leovegas.drugstore.mapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sigma.software.leovegas.drugstore.dto.OrderDto;
import sigma.software.leovegas.drugstore.persistence.entity.Order;
import sigma.software.leovegas.drugstore.persistence.entity.Product;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OrderMapperTest {

    private static final Order ORDER = new Order(1l, null, BigDecimal.valueOf(50.0));
    private static final OrderDto ORDER_DTO = new OrderDto(1L, null, BigDecimal.valueOf(50.0));
    private static final Product PRODUCT = new Product();
    private static final List<OrderDto> ORDER_DTO_LIST = new ArrayList<>();
    private static final List<Order> ORDER_LIST = new ArrayList<>();
    private static final List<Product> PRODUCT_LIST = new ArrayList<>();


    @BeforeEach
    void setUp() {
        PRODUCT_LIST.add(PRODUCT);
        ORDER_LIST.add(ORDER);
        ORDER_DTO_LIST.add(ORDER_DTO);
        ORDER.setProductList(PRODUCT_LIST);
        ORDER_DTO.setProductList(PRODUCT_LIST);

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
