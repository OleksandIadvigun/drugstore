package sigma.software.leovegas.drugstore.store.client

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
import sigma.software.leovegas.drugstore.store.client.proto.StoreClientProto

@Configuration
@ConditionalOnMissingClass
@EnableConfigurationProperties(StoreProperties::class)
class StoreClientConfiguration(private val messageConverters: ObjectFactory<HttpMessageConverters>) {

    @Bean
    @ConditionalOnMissingBean
    fun storeClient(props: StoreProperties): StoreClient =
        Feign
            .builder()
            .logger(Slf4jLogger())
            .logLevel(Logger.Level.FULL)
            .encoder(JacksonEncoder(listOf(JavaTimeModule())))
            .decoder(JacksonDecoder(listOf(JavaTimeModule())))
            .target(
                StoreClient::class.java,
                "http://${props.host}:${props.port}"
            )

    @Bean
    @ConditionalOnMissingBean
    fun storeClientProto(props: StoreProperties): StoreClientProto =
        Feign
            .builder()
            .logger(Slf4jLogger())
            .logLevel(Logger.Level.FULL)
            .encoder(SpringEncoder(messageConverters))
            .decoder(ResponseEntityDecoder(SpringDecoder(this.messageConverters)))
            .target(
                StoreClientProto::class.java,
                "http://${props.host}:${props.port}"
            )

    @Bean
    @ConditionalOnMissingBean
    fun storeProtobufHttpMessageConverterStore(): ProtobufHttpMessageConverter =
        ProtobufHttpMessageConverter()
}
