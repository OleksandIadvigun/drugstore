package sigma.software.leovegas.drugstore.order.client

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import feign.Feign
import feign.Logger
import feign.jackson.JacksonDecoder
import feign.jackson.JacksonEncoder
import feign.slf4j.Slf4jLogger
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.cloud.openfeign.support.PageJacksonModule
import org.springframework.cloud.openfeign.support.SortJacksonModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(OrderProperties::class)
class OrderClientConfiguration {

    @Bean
    fun OrderClient(props: OrderProperties): OrderClient {
        return Feign
            .builder()
            .logger(Slf4jLogger())
            .logLevel(Logger.Level.FULL)
            .encoder(JacksonEncoder(listOf(JavaTimeModule(), PageJacksonModule(), SortJacksonModule())))
            .decoder(JacksonDecoder(listOf(JavaTimeModule(), PageJacksonModule(), SortJacksonModule())))
            .target(
                OrderClient::class.java,
                "http://${props.host}:${props.port}"
            )
    }
}
