package sigma.software.leovegas.drugstore.order.client

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
import org.springframework.cloud.openfeign.support.PageJacksonModule
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder
import org.springframework.cloud.openfeign.support.SortJacksonModule
import org.springframework.cloud.openfeign.support.SpringDecoder
import org.springframework.cloud.openfeign.support.SpringEncoder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.protobuf.ProtobufHttpMessageConverter
import sigma.software.leovegas.drugstore.order.client.proto.OrderClientProto

@Configuration
@EnableConfigurationProperties(OrderProperties::class)
class OrderClientConfiguration @Autowired constructor(
    val messageConverters: ObjectFactory<HttpMessageConverters>,
) {

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

    @Bean
    fun OrderClientProto(props: OrderProperties): OrderClientProto {
        return Feign
            .builder()
            .logger(Slf4jLogger())
            .logLevel(Logger.Level.FULL)
            .encoder(SpringEncoder(messageConverters))
            .decoder(ResponseEntityDecoder(SpringDecoder(this.messageConverters)))
            .target(
                OrderClientProto::class.java,
                "http://${props.host}:${props.port}"
            )
    }

    @Bean
    fun protobufHttpMessageConverterOrder(): ProtobufHttpMessageConverter {
        return ProtobufHttpMessageConverter();
    }
}
