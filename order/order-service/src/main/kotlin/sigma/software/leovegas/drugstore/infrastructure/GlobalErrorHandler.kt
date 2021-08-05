package sigma.software.leovegas.drugstore.infrastructure

import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import sigma.software.leovegas.drugstore.infrastructure.extensions.toBadRequestResult

@ControllerAdvice
class GlobalErrorHandler {

    @ExceptionHandler(Throwable::class)
    fun handleNotFound(e: Throwable) =
        e.toBadRequestResult() // TODO: FIXME: Introduce logging and method / path for better dev.exp
}
