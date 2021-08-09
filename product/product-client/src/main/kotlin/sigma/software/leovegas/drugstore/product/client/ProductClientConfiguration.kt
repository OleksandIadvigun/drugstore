package sigma.software.leovegas.drugstore.product.client

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import feign.Feign
import feign.Logger
import feign.jackson.JacksonDecoder
import feign.jackson.JacksonEncoder
import feign.slf4j.Slf4jLogger
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ProductClientConfiguration {

    @Bean
    fun ProductClient(): ProductClient{
        return Feign
            .builder()
            .logger(Slf4jLogger())
            .logLevel(Logger.Level.FULL)
            .encoder(JacksonEncoder(listOf(JavaTimeModule())))
            .decoder(JacksonDecoder(listOf(JavaTimeModule())))
            .target(ProductClient::class.java, "http://localhost:8081")
    }
}