package sigma.software.leovegas.drugstore.infrastructure

import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity

fun <T : Throwable> T.toBadRequestResult() =
    ResponseEntity.badRequest()
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .body(ApiError(message))
