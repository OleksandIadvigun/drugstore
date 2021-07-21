package sigma.software.leovegas.drugstore.mapper;

import java.util.List;
import java.util.stream.Collectors;
import sigma.software.leovegas.drugstore.dto.OrderDetailsDto;

import sigma.software.leovegas.drugstore.persistence.entity.OrderDetails;
import sigma.software.leovegas.drugstore.persistence.entity.Product;

public class OrderDetailsMapper {

    public static OrderDetailsDto toRestDto(OrderDetails orderDetails) {
        return OrderDetailsDto.builder()
                .id(orderDetails.getId())
                .name(orderDetails.getProduct().getName())
                .price(orderDetails.getProduct().getPrice())
                .productId(orderDetails.getProduct().getId())
                .quantity(orderDetails.getQuantity())
                .build();
    }

    public static OrderDetails toEntity(OrderDetailsDto orderDetailsDto) {
        return OrderDetails.builder()
              .id(orderDetailsDto.getId())
                .product(new Product(orderDetailsDto.getProductId(), orderDetailsDto.getName(),
                        null,orderDetailsDto.getPrice()))
                .quantity(orderDetailsDto.getQuantity())
                .build();
    }

    public static List<OrderDetailsDto> toRestDto(List<OrderDetails> orderDetailsList) {
        return orderDetailsList.stream().map(OrderDetailsMapper::toRestDto).collect(Collectors.toList());
    }

    public static List<OrderDetails> toEntity(List<OrderDetailsDto> orderDetailsDtoList) {
        return orderDetailsDtoList.stream().map(OrderDetailsMapper::toEntity).collect(Collectors.toList());
    }
}
