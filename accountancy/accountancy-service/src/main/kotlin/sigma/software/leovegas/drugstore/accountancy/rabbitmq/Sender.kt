//package sigma.software.leovegas.drugstore.accountancy.rabbitmq
//
//import com.fasterxml.jackson.databind.ObjectMapper
//import java.util.logging.Logger
//import org.springframework.amqp.core.Message
//import org.springframework.amqp.core.MessageBuilder
//import org.springframework.amqp.core.MessageProperties
//import org.springframework.amqp.core.TopicExchange
//import org.springframework.amqp.rabbit.core.RabbitTemplate
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.stereotype.Component
//import sigma.software.leovegas.drugstore.product.api.CreateProductRequest
//
//@Component
//class Sender @Autowired constructor(
//    val rabbitTemplate: RabbitTemplate,
//    val queueName: String,
//    val objectMapper: ObjectMapper,
//    val exchange: TopicExchange
//) {
//
//    var logger = Logger.getLogger(Sender::class.java.name)
//
//    fun send(list: List<CreateProductRequest>) {
//        val json = objectMapper.writeValueAsString(list)
//        val message: Message = MessageBuilder
//            .withBody(json.toByteArray())
//            .setContentType(MessageProperties.CONTENT_TYPE_JSON)
//            .build()
//        logger.info("sending a message...")
//        rabbitTemplate.setExchange(exchange.name)
//        rabbitTemplate.convertAndSend(queueName, "content: $message")
//    }
//}