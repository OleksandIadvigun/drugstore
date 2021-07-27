package sigma.software.leovegas.drugstore.exception.handlers

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import sigma.software.leovegas.drugstore.exception.ApiExceptionModel
import sigma.software.leovegas.drugstore.exception.InsufficientAmountOfProductForOrderException
import sigma.software.leovegas.drugstore.exception.OrderNotFoundException
import sigma.software.leovegas.drugstore.exception.ResourceNotFoundException
import java.time.LocalDateTime


@ControllerAdvice
class RestRespEntityExceptionHandler : ResponseEntityExceptionHandler() {

    @ExceptionHandler(value = [ResourceNotFoundException::class])
    fun handleNotFound(ex: RuntimeException, request: WebRequest): ResponseEntity<Any> {
        val apiExceptionModel = ApiExceptionModel(ex.message, LocalDateTime.now())
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        return handleExceptionInternal(ex, apiExceptionModel, headers, HttpStatus.NOT_FOUND, request)
    }

    @ExceptionHandler(value = [OrderNotFoundException::class])
    fun handleOrderNotFound(ex: Exception, request: WebRequest): ResponseEntity<Any> {
        val apiExceptionModel = ApiExceptionModel(ex.message, LocalDateTime.now())
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        return handleExceptionInternal(ex, apiExceptionModel, headers, HttpStatus.NOT_FOUND, request)
    }

    @ExceptionHandler(value = [InsufficientAmountOfProductForOrderException::class])
    fun handleInsufficientProduct(ex: Exception, request: WebRequest): ResponseEntity<Any> {
        val apiExceptionModel = ApiExceptionModel(ex.message, LocalDateTime.now())
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        return handleExceptionInternal(ex, apiExceptionModel, headers, HttpStatus.BAD_REQUEST, request)
    }
}

