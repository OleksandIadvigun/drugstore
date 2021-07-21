package sigma.software.leovegas.drugstore.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import sigma.software.leovegas.drugstore.dto.OrderDetailsDto;
import sigma.software.leovegas.drugstore.dto.OrderDto;
import sigma.software.leovegas.drugstore.exception.InsufficientProductAmountException;
import sigma.software.leovegas.drugstore.exception.NoOrdersFoundException;
import sigma.software.leovegas.drugstore.exception.OrderNotFoundException;
import sigma.software.leovegas.drugstore.service.OrderService;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public @ResponseBody
    OrderDto postOrder(@RequestBody List<OrderDetailsDto> orderDetailsDtoList) throws InsufficientProductAmountException {
        return orderService.postOrder(orderDetailsDtoList);
    }

    @DeleteMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public @ResponseBody
    OrderDto cancelOrder(@PathVariable("id") Long id) throws OrderNotFoundException {
        return orderService.cancelOrderById(id);
    }

    @GetMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    OrderDto getOrder(@PathVariable("id") Long id) throws OrderNotFoundException {
        return orderService.getOrderById(id);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    List<OrderDto> getAllOrders() throws NoOrdersFoundException {
        return orderService.getAllOrders();
    }

    @PostMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public @ResponseBody
    OrderDto getAllOrders(@PathVariable("id") Long id, @RequestBody List<OrderDetailsDto> orderDetailsDtoList)
            throws OrderNotFoundException, InsufficientProductAmountException {
        return orderService.updateOrder(id, orderDetailsDtoList);
    }
}
