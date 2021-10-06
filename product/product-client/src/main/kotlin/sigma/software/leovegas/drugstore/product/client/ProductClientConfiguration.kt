package sigma.software.leovegas.drugstore.product.client

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import feign.Feign
import feign.Logger
import feign.jackson.JacksonDecoder
import feign.jackson.JacksonEncoder
import feign.slf4j.Slf4jLogger
import org.springframework.beans.factory.ObjectFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass
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
@ConditionalOnMissingClass
@EnableConfigurationProperties(ProductProperties::class)
class ProductClientConfiguration(val messageConverters: ObjectFactory<HttpMessageConverters>) {

    @Bean
    @ConditionalOnMissingBean
    fun productClient(props: ProductProperties): ProductClient =
        Feign
            .builder()
            .logger(Slf4jLogger())
            .logLevel(Logger.Level.FULL)
            .encoder(JacksonEncoder(listOf(JavaTimeModule())))
            .decoder(JacksonDecoder(listOf(JavaTimeModule())))
            .target(ProductClient::class.java, "http://${props.host}:${props.port}")

    @Bean
    @ConditionalOnMissingBean
    fun productClientProto(
        props: ProductProperties,
        productSpringEncoder: SpringEncoder,
        productSpringDecoder: ResponseEntityDecoder,
    ): ProductClientProto =
        Feign
            .builder()
            .logger(Slf4jLogger())
            .logLevel(Logger.Level.FULL)
            .encoder(productSpringEncoder)
            .decoder(productSpringDecoder)
            .target(ProductClientProto::class.java, "http://${props.host}:${props.port}")

    @Bean
    fun productProtobufHttpMessageConverter(): ProtobufHttpMessageConverter =
        ProtobufHttpMessageConverter()

    @Bean
    @ConditionalOnMissingBean
    fun productSpringEncoder(): SpringEncoder =
        SpringEncoder(this.messageConverters)

    @Bean
    @ConditionalOnMissingBean
    fun productSpringDecoder(): ResponseEntityDecoder =
        ResponseEntityDecoder(SpringDecoder(this.messageConverters))
}
