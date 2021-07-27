package sigma.software.leovegas.drugstore.order

import com.fasterxml.jackson.databind.ObjectMapper
import java.math.BigDecimal

data class OrderResponseBuilder(
    var id: Long = 0L,
    var orderDetailsList: List<OrderDetailsResponse> = listOf(),
    var total: BigDecimal = BigDecimal.ZERO
)

fun OrderResponseBuilder.build() =
    OrderResponse(id, orderDetailsList, total)

fun ObjectMapper.json(block: OrderResponseBuilder.() -> Unit = {}): String =
    OrderResponseBuilder().run {
        block(this)
        writerWithDefaultPrettyPrinter()
            .writeValueAsString(build())
    }

data class OrderDetailsRequestBuilder(
    var productId: Long = 0L,
    var quantity: Int = 0,
)

fun OrderDetailsRequestBuilder.build() = OrderDetailsRequest(productId, quantity)

fun OrderDetailsRequest(block: OrderDetailsRequestBuilder.() -> Unit = {}): OrderDetailsRequest =
    OrderDetailsRequestBuilder().run {
        block(this)
        build()
    }

fun ObjectMapper.json(vararg requests: OrderDetailsRequest) =
    writerWithDefaultPrettyPrinter()
        .writeValueAsString(
            OrderRequest(
                requests.toList()
            )
        )

