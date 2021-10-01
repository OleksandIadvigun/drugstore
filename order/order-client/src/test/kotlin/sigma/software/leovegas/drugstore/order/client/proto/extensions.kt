package sigma.software.leovegas.drugstore.order.client.proto

import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.client.WireMock.binaryEqualTo
import com.github.tomakehurst.wiremock.client.WireMock.containing

import com.google.protobuf.MessageLite

fun MappingBuilder.withProtobufRequest(f: () -> MessageLite): MappingBuilder =
    withHeader("Content-Type", containing("application/x-protobuf"))
        .withRequestBody(binaryEqualTo(f().toByteArray()))

fun ResponseDefinitionBuilder.withProtobufResponse(f: () -> MessageLite): ResponseDefinitionBuilder =
    withHeader("Content-Type", "application/x-protobuf")
        .withBody(f.invoke().toByteArray())
