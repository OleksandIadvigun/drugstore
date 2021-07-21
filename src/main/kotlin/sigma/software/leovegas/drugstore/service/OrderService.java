package sigma.software.leovegas.drugstore.service;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sigma.software.leovegas.drugstore.dto.OrderDetailsDto;
import sigma.software.leovegas.drugstore.dto.OrderDto;
import sigma.software.leovegas.drugstore.exception.InsufficientProductAmountException;
import sigma.software.leovegas.drugstore.exception.NoOrdersFoundException;
import sigma.software.leovegas.drugstore.exception.OrderNotFoundException;
import sigma.software.leovegas.drugstore.mapper.OrderDetailsMapper;
import sigma.software.leovegas.drugstore.mapper.OrderMapper;
import sigma.software.leovegas.drugstore.persistence.entity.Order;
import sigma.software.leovegas.drugstore.persistence.repository.OrderRepository;

@Service
public class OrderService {

    @Autowired
    OrderRepository orderRepository;

    @Transactional
    public List<OrderDto> getAllOrders() throws NoOrdersFoundException {
        List<OrderDto> orderList = OrderMapper.toRestDto(orderRepository.findAll());
        if (orderList.isEmpty()) {
            throw new NoOrdersFoundException();
        }
        return orderList;
    }

    @Transactional
    public OrderDto getOrderById(Long id) throws OrderNotFoundException {
        return OrderMapper.toRestDto(orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id)));
    }

    @Transactional
    public OrderDto cancelOrderById(Long id) throws OrderNotFoundException {
        OrderDto order = getOrderById(id);
        orderRepository.deleteById(id);
        return order;
    }
    @Transactional
    public OrderDto postOrder(List<OrderDetailsDto> orderDetailsDtoList) throws InsufficientProductAmountException {
        if (orderDetailsDtoList.isEmpty()) {
            throw new InsufficientProductAmountException();
        }
        Order order = new Order();
        order.setOrderDetailsList(OrderDetailsMapper.toEntity(orderDetailsDtoList));
        order.setTotal(calculateTotalValue(orderDetailsDtoList));
        Order orderToSave = orderRepository.save(order);
        return OrderMapper.toRestDto(orderToSave);
    }
    @Transactional
    public OrderDto updateOrder(Long id, List<OrderDetailsDto> orderDetailsDtoList) throws OrderNotFoundException, InsufficientProductAmountException {
        Order order = orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
        if (orderDetailsDtoList.isEmpty()) {
            throw new InsufficientProductAmountException();
        }
        order.setOrderDetailsList(OrderDetailsMapper.toEntity(orderDetailsDtoList));
        order.setTotal(calculateTotalValue(orderDetailsDtoList));
        Order orderToSave = orderRepository.save(order);
        return OrderMapper.toRestDto(orderToSave);

    }

    private BigDecimal calculateTotalValue(List<OrderDetailsDto> orderDetailsDtoList) {
        BigDecimal total = BigDecimal.valueOf(0.0);
        for (OrderDetailsDto orderDetailsDto : orderDetailsDtoList) {
            total = total.add(orderDetailsDto.getPrice().multiply(BigDecimal.valueOf(orderDetailsDto.getQuantity())));
        }
        return total;
    }
}
