package sigma.software.leovegas.drugstore.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Order Controller", description = "Allows to create/cancel/change/get orders")
public class OrderController {

    @Autowired
    OrderService orderService;

    @Operation(summary = "Create Order", description = "Allows to create order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "order created",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = OrderDto.class))),
            @ApiResponse(responseCode = "400", description = "Insufficient amount of product in the order",
                    content = @Content(mediaType = "text/plain"))
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public @ResponseBody
    OrderDto postOrder(@RequestBody
                       @io.swagger.v3.oas.annotations.parameters.RequestBody(
                               description = "currency amount and name",
                               content = @Content(array = @ArraySchema(schema = @Schema(implementation = OrderDetailsDto.class))))
                               List<OrderDetailsDto> orderDetailsDtoList)
            throws InsufficientProductAmountException {
        return orderService.postOrder(orderDetailsDtoList);
    }

    @Operation(summary = "Allows to cancel the order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "The order has been successfully cancelled",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = OrderDto.class))),
            @ApiResponse(responseCode = "404", description = "Order was not found",
                    content = @Content(mediaType = "text/plain"))
    })
    @DeleteMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public @ResponseBody
    OrderDto cancelOrder(@PathVariable("id") @Parameter(
            description = "id of the order",
            content = @Content(schema = @Schema(implementation = Long.class))) Long id)
            throws OrderNotFoundException {
        return orderService.cancelOrderById(id);
    }


    @Operation(summary = "Get order by id", description = "Allows to obtain information about order by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "order is obtained",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = OrderDto.class))),
            @ApiResponse(responseCode = "404", description = "Order was not found",
                    content = @Content(mediaType = "text/plain"))
    })
    @GetMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    OrderDto getOrder(@PathVariable("id") @Parameter(
            description = "id of the order",
            content = @Content(schema = @Schema(implementation = Long.class))) Long id)
            throws OrderNotFoundException {
        return orderService.getOrderById(id);
    }

    @Operation(summary = "Get all orders", description = "Allows to obtain information about all orders")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "orders are obtained",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(
                            schema = @Schema(implementation = OrderDto.class)))),
            @ApiResponse(responseCode = "404", description = "No  orders were found",
                    content = @Content(mediaType = "text/plain"))
    })
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    List<OrderDto> getAllOrders() throws NoOrdersFoundException {
        return orderService.getAllOrders();
    }

    @Operation(summary = "Change  order by id", description = "Allows to change order by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "order is changed",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = OrderDto.class))),
            @ApiResponse(responseCode = "404", description = "Order was not found",
                    content = @Content(mediaType = "text/plain"))
    })
    @PostMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public @ResponseBody
    OrderDto updateOrder(@PathVariable("id") @Parameter(description = "id of the order",
            content = @Content(schema = @Schema(implementation = Long.class))) Long id,
                         @RequestBody @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                 description = "currency amount and name",
                                 content = @Content(array = @ArraySchema(
                                         schema = @Schema(implementation = OrderDetailsDto.class))))
                                 List<OrderDetailsDto> orderDetailsDtoList)
            throws OrderNotFoundException, InsufficientProductAmountException {
        return orderService.updateOrder(id, orderDetailsDtoList);
    }
}
