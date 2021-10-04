package sigma.software.leovegas.drugstore.api

import com.google.protobuf.ByteString
import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext
import sigma.software.leovegas.drugstore.api.protobuf.Proto

fun String.messageSpliterator() = run {
    val step1 = split(":", ignoreCase = true, limit = 0)
    if (step1.size < 5) {
        step1.last().split(".").get(0)
    } else
        step1.get(6).split(".").get(0).substring(1)
}

fun BigDecimal.toDecimalProto() = Proto.DecimalValue.newBuilder()
    .setPrecision(this.precision())
    .setScale(this.scale())
    .setValue(ByteString.copyFrom(this.unscaledValue().toByteArray()))
    .build()

fun Proto.DecimalValue.toBigDecimal() = BigDecimal(
    BigInteger(this.value.toByteArray()),
    this.scale,
    MathContext(this.precision)
)
