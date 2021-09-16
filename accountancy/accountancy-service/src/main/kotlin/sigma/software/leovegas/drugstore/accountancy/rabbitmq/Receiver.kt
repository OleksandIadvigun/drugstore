//package sigma.software.leovegas.drugstore.accountancy.rabbitmq
//
//import java.util.concurrent.CountDownLatch
//import java.util.logging.Logger
//import org.springframework.stereotype.Component
//
//@Component
//class Receiver() {
//
//    var logger = Logger.getLogger(Receiver::class.java.name)
//
//    fun receive(message: String) {
//        logger.info("received new message! $message")
//    }
//
//    fun defaultListenerMethod(): String {
//        return "receive"
//    }
//
//    fun getLatch(): CountDownLatch {
//        return CountDownLatch(1)
//    }
//}