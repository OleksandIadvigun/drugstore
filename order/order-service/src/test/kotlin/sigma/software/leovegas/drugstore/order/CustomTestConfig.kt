package sigma.software.leovegas.drugstore.order

import com.github.fridujo.rabbitmq.mock.MockConnectionFactory
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
internal class CustomTestConfig {
    @Bean
    fun connectionFactory(): ConnectionFactory {
        return CachingConnectionFactory(MockConnectionFactory())
    }
}
