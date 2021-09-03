package sigma.software.leovegas.drugstore.product

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest

internal class RestResponsePage<T> : PageImpl<T> {
    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    constructor(
        @JsonProperty("content") content: List<T> = listOf(),
        @JsonProperty("number") number: Int,
        @JsonProperty("size") size: Int,
        @JsonProperty("totalElements") totalElements: Long?,
        @JsonProperty("pageable") pageable: JsonNode?,
        @JsonProperty("last") last: Boolean,
        @JsonProperty("totalPages") totalPages: Int,
        @JsonProperty("sort") sort: JsonNode?,
        @JsonProperty("first") first: Boolean,
        @JsonProperty("numberOfElements") numberOfElements: Int
    ) : super(
        content!!, PageRequest.of(number, size),
        totalElements!!
    )
}
