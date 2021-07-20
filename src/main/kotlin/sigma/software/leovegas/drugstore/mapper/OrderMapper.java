package sigma.software.leovegas.drugstore.mapper;

import java.util.List;
import java.util.stream.Collectors;
import sigma.software.leovegas.drugstore.dto.OrderDto;
import sigma.software.leovegas.drugstore.persistence.entity.Order;

public class OrderMapper {

    public static OrderDto toRestDto(Order order) {
        return OrderDto.builder()
                .id(order.getId())
                .productList(order.getProductList())
                .total(order.getTotal())
                .build();
    }

    public static Order toEntity(OrderDto orderDto) {
        return Order.builder()
                .id(orderDto.getId())
                .productList(orderDto.getProductList())
                .total(orderDto.getTotal())
                .build();
    }

    public static List<OrderDto> toRestDto(List<Order> orderList) {
        return orderList.stream().map(OrderMapper::toRestDto).collect(Collectors.toList());
    }
}