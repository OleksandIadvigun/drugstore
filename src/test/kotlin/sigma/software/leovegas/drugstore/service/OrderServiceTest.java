package sigma.software.leovegas.drugstore.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sigma.software.leovegas.drugstore.dto.OrderDetailsDto;
import sigma.software.leovegas.drugstore.dto.OrderDto;
import sigma.software.leovegas.drugstore.exception.InsufficientProductAmountException;
import sigma.software.leovegas.drugstore.exception.NoOrdersFoundException;
import sigma.software.leovegas.drugstore.exception.OrderNotFoundException;
import sigma.software.leovegas.drugstore.persistence.entity.Order;
import sigma.software.leovegas.drugstore.persistence.entity.OrderDetails;
import sigma.software.leovegas.drugstore.persistence.entity.Product;
import sigma.software.leovegas.drugstore.persistence.repository.OrderRepository;


import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    private static final List<OrderDto> ORDER_DTO_LIST = new ArrayList<>();
    private static final List<Order> ORDER_LIST = new ArrayList<>();
    private static final OrderDto ORDER_DTO = new OrderDto();
    private static final OrderDto ORDER_DTO_TO_SAVE = new OrderDto();
    private static final Order ORDER = new Order();
    private static final Order ORDER_TO_SAVE = new Order();
    private static final Long ID_1 = 1L;
    private static final Long ID_2 = 2L;
    private static final Integer QUANTITY = 2;
    private static final BigDecimal PRICE_1 = BigDecimal.valueOf(20.0);
    private static final BigDecimal PRICE_2 = BigDecimal.valueOf(30.0);
    private static final BigDecimal TOTAL_2 = BigDecimal.valueOf(60.0);
    private static final BigDecimal TOTAL_1 = BigDecimal.valueOf(40.0);
    private static final Product PRODUCT_1 = new Product(ID_1, "product1", null, PRICE_1);
    private static final Product PRODUCT_2 = new Product(ID_2, "product2", null, PRICE_2);

    private static final OrderDetails ORDER_DETAILS_1 = new OrderDetails(ID_1, PRODUCT_1, QUANTITY);
    private static final OrderDetails ORDER_DETAILS_2 = new OrderDetails(ID_1, PRODUCT_2, QUANTITY);
    private static final OrderDetailsDto ORDER_DETAILS_DTO_1 = new OrderDetailsDto(
            ID_1, PRODUCT_1.getId(), PRODUCT_1.getName(), PRODUCT_1.getPrice(), QUANTITY);
    private static final OrderDetailsDto ORDER_DETAILS_DTO_2 = new OrderDetailsDto(
            ID_1, PRODUCT_1.getId(), PRODUCT_1.getName(), PRODUCT_2.getPrice(), QUANTITY);
    private static final List<OrderDetails> ORDER_DETAILS_LIST_1 = new ArrayList<>();
    private static final List<OrderDetailsDto> ORDER_DETAILS_DTO_LIST_1 = new ArrayList<>();
    private static final List<OrderDetails> ORDER_DETAILS_LIST_2 = new ArrayList<>();
    private static final List<OrderDetailsDto> ORDER_DETAILS_DTO_LIST_2 = new ArrayList<>();
    @Mock
    private OrderRepository orderRepository;


    @InjectMocks
    private OrderService orderService;

    @BeforeAll
    static void init() {
        ORDER_DETAILS_LIST_1.add(ORDER_DETAILS_1);
        ORDER_DETAILS_DTO_LIST_1.add(ORDER_DETAILS_DTO_1);
        ORDER_DETAILS_LIST_2.add(ORDER_DETAILS_2);
        ORDER_DETAILS_DTO_LIST_2.add(ORDER_DETAILS_DTO_2);
    }

    @BeforeEach
    void setUp() {
        ORDER.setId(ID_1);
        ORDER.setTotal(TOTAL_1);
        ORDER.setOrderDetailsList(ORDER_DETAILS_LIST_1);

        ORDER_DTO.setId(ID_1);
        ORDER_DTO.setTotal(TOTAL_1);
        ORDER_DTO.setOrderDetailsDtoList(ORDER_DETAILS_DTO_LIST_1);

        ORDER_TO_SAVE.setOrderDetailsList(ORDER_DETAILS_LIST_1);
        ORDER_TO_SAVE.setTotal(TOTAL_1);

        ORDER_DTO_TO_SAVE.setOrderDetailsDtoList(ORDER_DETAILS_DTO_LIST_1);
        ORDER_DTO_TO_SAVE.setTotal(TOTAL_1);
    }


    @Test
    void getAllOrders() throws NoOrdersFoundException {
        ORDER_DTO_LIST.add(ORDER_DTO);
        ORDER_LIST.add(ORDER);
        when(orderRepository.findAll()).thenReturn(ORDER_LIST);
        assertEquals(ORDER_DTO_LIST, orderService.getAllOrders());
    }

    @Test
    void OrdersNotFoundException() {
        when(orderRepository.findAll()).thenReturn(Collections.emptyList());
        assertThrows(NoOrdersFoundException.class, () -> orderService.getAllOrders());
    }


    @Test
    void getOrderById() throws OrderNotFoundException {
        when(orderRepository.findById(ID_1)).thenReturn(Optional.of(ORDER));
        assertEquals(ORDER_DTO, orderService.getOrderById(ID_1));
    }

    @Test
    void OrderToFindNotFoundException() {
        when(orderRepository.findById(ID_1)).thenReturn(Optional.empty());
        assertThrows(OrderNotFoundException.class, () -> orderService.getOrderById(ID_1));
    }

    @Test
    void cancelOrderById() throws OrderNotFoundException {
        when(orderRepository.findById(ID_1)).thenReturn(Optional.of(ORDER));
        assertEquals(ORDER_DTO, orderService.cancelOrderById(ID_1));
    }

    @Test
    void OrderToCancelNotFoundException() {
        when(orderRepository.findById(ID_1)).thenReturn(Optional.empty());
        assertThrows(OrderNotFoundException.class, () -> orderService.cancelOrderById(ID_1));
    }

    @Test
    void postOrder() throws InsufficientProductAmountException {

        when(orderRepository.save(ORDER_TO_SAVE)).thenReturn(ORDER_TO_SAVE);
        assertEquals(ORDER_DTO_TO_SAVE, orderService.postOrder(ORDER_DETAILS_DTO_LIST_1));
    }

    @Test
    void InsufficientProductAmountException() {
        assertThrows(InsufficientProductAmountException.class, () -> orderService.postOrder(Collections.emptyList()));
    }

    @Test
    void updateOrder() throws OrderNotFoundException, InsufficientProductAmountException {
        ORDER_TO_SAVE.setOrderDetailsList(ORDER_DETAILS_LIST_2);
        ORDER_TO_SAVE.setTotal(TOTAL_2);
        ORDER_DTO_TO_SAVE.setOrderDetailsDtoList(ORDER_DETAILS_DTO_LIST_2);
        ORDER_DTO_TO_SAVE.setTotal(TOTAL_2);
        when(orderRepository.findById(ID_1)).thenReturn(Optional.of(ORDER_TO_SAVE));
        when(orderRepository.save(ORDER_TO_SAVE)).thenReturn(ORDER_TO_SAVE);
        assertEquals(ORDER_DTO_TO_SAVE, orderService.updateOrder(ID_1,ORDER_DETAILS_DTO_LIST_2));
    }

    @Test
    void InsufficientProductAmountToUpdateOrderException() {
        when(orderRepository.findById(ID_1)).thenReturn(Optional.of(ORDER_TO_SAVE));
        assertThrows(InsufficientProductAmountException.class, () -> orderService.updateOrder(ID_1,Collections.emptyList()));
    }
    @Test
    void OrderToUpdateNotFoundException() {
        assertThrows(OrderNotFoundException.class, () -> orderService.updateOrder(null,Collections.emptyList()));
    }
}
