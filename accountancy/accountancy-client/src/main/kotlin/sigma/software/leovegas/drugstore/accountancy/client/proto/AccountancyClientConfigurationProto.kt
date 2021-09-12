package sigma.software.leovegas.drugstore.accountancy.client.proto

import feign.Feign
import feign.Logger
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
import sigma.software.leovegas.drugstore.accountancy.client.AccountancyProperties

@Configuration
@EnableConfigurationProperties(AccountancyProperties::class)
class AccountancyClientConfigurationProto @Autowired constructor(
    val messageConverters: ObjectFactory<HttpMessageConverters>
)
{

    @Bean
    fun AccountancyClientProto(props: AccountancyProperties): AccountancyClientProto {
        return Feign
            .builder()
            .encoder(springEncoder())
            .decoder(springDecoder())
            .logger(Slf4jLogger())
            .logLevel(Logger.Level.FULL)
            .target(AccountancyClientProto::class.java, "http://${props.host}:${props.port}")
    }

    //add the protobuf http message converter
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
        return  ResponseEntityDecoder( SpringDecoder(this.messageConverters));
    }
}