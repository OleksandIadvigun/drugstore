package sigma.software.leovegas.drugstore.product.client

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import feign.Feign
import feign.Logger
import feign.jackson.JacksonDecoder
import feign.jackson.JacksonEncoder
import feign.slf4j.Slf4jLogger
import org.springframework.beans.factory.ObjectFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.http.HttpMessageConverters
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder
import org.springframework.cloud.openfeign.support.SpringDecoder
import org.springframework.cloud.openfeign.support.SpringEncoder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.protobuf.ProtobufHttpMessageConverter
import sigma.software.leovegas.drugstore.product.client.proto.ProductClientProto


@Configuration
@EnableConfigurationProperties(ProductProperties::class)
class ProductClientConfiguration @Autowired constructor(
    val messageConverters: ObjectFactory<HttpMessageConverters>
) {

    @Bean
    fun ProductClient(props: ProductProperties): ProductClient {
        return Feign
            .builder()
            .logger(Slf4jLogger())
            .logLevel(Logger.Level.FULL)
            .encoder(JacksonEncoder(listOf(JavaTimeModule())))
            .decoder(JacksonDecoder(listOf(JavaTimeModule())))
            .target(ProductClient::class.java, "http://${props.host}:${props.port}")
    }

    @Bean
    fun ProductClientProto(props: ProductProperties): ProductClientProto {
        return Feign
            .builder()
            .logger(Slf4jLogger())
            .logLevel(Logger.Level.FULL)
            .encoder(springEncoder())
            .decoder(springDecoder())
            .target(ProductClientProto::class.java, "http://${props.host}:${props.port}")

    }

    @Bean
    fun protobufHttpMessageConverter(): ProtobufHttpMessageConverter {
        return ProtobufHttpMessageConverter();
    }

    //override the encoder
    @Bean
    fun springEncoder(): SpringEncoder {
        return SpringEncoder(this.messageConverters)
    }

    //override the encoder
    @Bean
    fun springDecoder(): ResponseEntityDecoder {
        return ResponseEntityDecoder(SpringDecoder(this.messageConverters));
    }
}
