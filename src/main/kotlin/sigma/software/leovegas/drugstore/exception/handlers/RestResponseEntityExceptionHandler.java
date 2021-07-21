package sigma.software.leovegas.drugstore.exception.handlers;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;


import java.time.LocalDateTime;
import sigma.software.leovegas.drugstore.exception.ApiExceptionModel;
import sigma.software.leovegas.drugstore.exception.ResourceNotFoundException;

@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = {ResourceNotFoundException.class})
    protected ResponseEntity<Object> handleNotFound(RuntimeException ex, WebRequest request) {
        ApiExceptionModel apiExceptionModel = new ApiExceptionModel(ex.getMessage(), LocalDateTime.now());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return handleExceptionInternal(ex, apiExceptionModel,
                headers, HttpStatus.NOT_FOUND, request);
    }
}
