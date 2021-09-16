package sigma.software.leovegas.drugstore.product

import java.util.function.Consumer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import sigma.software.leovegas.drugstore.product.api.CreateProductsEvent

@Configuration
class ProductEventHandler(
    val productService: ProductService,

    ) {

    @Bean
    fun createProductEventHandler() = Consumer<CreateProductsEvent> {
        productService.createProduct(it)
    }

}
