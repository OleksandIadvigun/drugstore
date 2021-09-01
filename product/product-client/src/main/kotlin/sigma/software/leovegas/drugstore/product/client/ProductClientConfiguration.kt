package sigma.software.leovegas.drugstore.product.client

import com.fasterxml.jackson.databind.Module
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
@EnableConfigurationProperties(ProductProperties::class)
class ProductClientConfiguration {

    @Bean
    fun ProductClient(props: ProductProperties): ProductClient {
        return Feign
            .builder()
            .logger(Slf4jLogger())
            .logLevel(Logger.Level.FULL)
            .encoder(JacksonEncoder(listOf(JavaTimeModule(),PageJacksonModule(),SortJacksonModule())))
            .decoder(JacksonDecoder(listOf(JavaTimeModule(),PageJacksonModule(),SortJacksonModule())))
            .target(ProductClient::class.java, "http://${props.host}:${props.port}")

    }
}
