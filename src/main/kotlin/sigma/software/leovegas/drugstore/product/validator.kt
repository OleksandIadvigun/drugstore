package sigma.software.leovegas.drugstore.product

import java.math.BigDecimal

fun ProductRequest.validate() {
    if (name.length > 250) {
        throw NotCorrectRequestException("Not correct request, field name length should be lower than 250!")
    }
    if (price > BigDecimal("10000000")) {
        throw NotCorrectRequestException("Not correct request, price should be lower than 10000000!")
    }
}
