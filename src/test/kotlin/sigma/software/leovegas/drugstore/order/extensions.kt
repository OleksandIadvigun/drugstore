package sigma.software.leovegas.drugstore.order

import com.fasterxml.jackson.databind.ObjectMapper

data class OrderResponseBuilder(
    var id: Long = 0L,
    var orderItems: Set<OrderItem> = setOf(),
)

fun OrderResponseBuilder.build() =
    OrderResponse(id, orderItems)

fun ObjectMapper.json(block: OrderResponseBuilder.() -> Unit = {}): String =
    OrderResponseBuilder().run {
        block(this)
        writerWithDefaultPrettyPrinter()
            .writeValueAsString(build())
    }

data class OrderItemBuilder(
    var productId: Long = 0L,
    var quantity: Int = 0,
)

fun OrderItemBuilder.build() = OrderItem(productId=productId, quantity = quantity)

fun OrderItem(block: OrderItemBuilder.() -> Unit = {}): OrderItem =
    OrderItemBuilder().run {
        block(this)
        build()
    }

fun ObjectMapper.json(vararg requests: OrderItem) =
    writerWithDefaultPrettyPrinter()
        .writeValueAsString(
            OrderRequest(
                requests.toSet()
            )
        )

