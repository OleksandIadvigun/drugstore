package sigma.software.leovegas.drugstore.accountancy

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.http.converter.protobuf.ProtobufHttpMessageConverter
import org.springframework.web.client.RestTemplate


@TestConfiguration
class RestBean {
    @Bean
    fun protobufHttpMessageConverter(): ProtobufHttpMessageConverter {
        return ProtobufHttpMessageConverter()
    }

    @Bean
    fun restTemplate(protobufHttpMessageConverter: ProtobufHttpMessageConverter?): RestTemplate {
        return RestTemplate(listOf(protobufHttpMessageConverter))
    }
}