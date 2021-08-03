package sigma.software.leovegas.drugstore.infrastructure

import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

 @ControllerAdvice
 class GlobalErrorHandler {

     @ExceptionHandler(Throwable::class)
     fun handleNotFound(e: Throwable) =
         e.toBadRequestResult()
 }
