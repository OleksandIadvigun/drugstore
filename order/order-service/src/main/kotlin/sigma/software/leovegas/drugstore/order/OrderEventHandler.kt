package sigma.software.leovegas.drugstore.order

import java.util.function.Consumer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import sigma.software.leovegas.drugstore.order.api.CreateOrderEvent
import sigma.software.leovegas.drugstore.order.api.UpdateOrderEvent

@Configuration
class OrderEventHandler(val orderService: OrderService) {

    @Bean
    fun createOrderEventHandler() = Consumer<CreateOrderEvent> {
        orderService.createOrder(it)
    }


    @Bean
    fun updateOrderEventHandler() = Consumer<UpdateOrderEvent> {
        orderService.updateOrder(it)
    }

}
