//package sigma.software.leovegas.drugstore.accountancy.rabbitmq
//
//import org.slf4j.Logger
//import org.slf4j.LoggerFactory
//import org.springframework.amqp.core.Binding
//import org.springframework.amqp.core.BindingBuilder
//import org.springframework.amqp.core.MessageListener
//import org.springframework.amqp.core.Queue
//import org.springframework.amqp.core.TopicExchange
//import org.springframework.amqp.rabbit.connection.CachingConnectionFactory
//import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer
//import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter
//import org.springframework.context.annotation.Bean
//import org.springframework.context.annotation.Configuration
//
//@Configuration
//class RabbitConf {
//    companion object {
//        val EXCHANGE_NAME = "drugstore_exchange"
//    }
//
//    val logger: Logger = LoggerFactory.getLogger(RabbitConf::class.java)
//
//    @Bean
//    fun queue(queueName: String): Queue {
//        return Queue(queueName, false)
//    }
//
//    @Bean
//    fun exchange(): TopicExchange {
//        return TopicExchange(EXCHANGE_NAME)
//    }
//
//    @Bean
//    fun binding(queue: Queue, exchange: TopicExchange): Binding {
//        return BindingBuilder.bind(queue).to(exchange).with("accountancy_queue.#")
//    }
//
//    @Bean
//    fun connectionFactory(): CachingConnectionFactory = CachingConnectionFactory("localhost")
//
//    @Bean
//    fun container(
//        connectionFactory: CachingConnectionFactory,
//        messageListener: MessageListener,
//        queue: Queue
//    ): SimpleMessageListenerContainer {
//        val container = SimpleMessageListenerContainer()
//        container.connectionFactory = connectionFactory
//        container.setQueueNames(queue.name)
//        container.setMessageListener(messageListener)
//        return container
//    }
//
//    @Bean
//    fun messageListener(receiver: Receiver): MessageListener {
//        return MessageListenerAdapter(receiver, receiver.defaultListenerMethod())
//    }
//
//    @Bean
//    fun queueName(): String {
//        return "accountancy_queue"
//    }
//
//}