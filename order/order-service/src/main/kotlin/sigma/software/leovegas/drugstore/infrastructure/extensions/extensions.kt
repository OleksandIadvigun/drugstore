package sigma.software.leovegas.drugstore.infrastructure.extensions

import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import sigma.software.leovegas.drugstore.api.ApiError

fun <T : Throwable> T.toBadRequestResult() = HttpStatus.BAD_REQUEST.run {
    ResponseEntity.status(this)
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .body(ApiError(value(), this.name, message))
}
