package sigma.software.leovegas.drugstore.store.api

// Requests

data class TransferCertificateRequest(
    val orderNumber: String = "undefined",
    val status: TransferStatusDTO = TransferStatusDTO.NONE,
    val comment: String = "undefined",
)

// Responses

data class TransferCertificateResponse(
    val certificateNumber: String = "undefined",
    val orderNumber: String = "undefined",
    val status: TransferStatusDTO = TransferStatusDTO.NONE,
    val comment: String = "undefined"
)

data class CheckStatusResponse(
    val orderNumber: String = "undefined",
    val comment: String = "undefined"
)

// DTOs

enum class TransferStatusDTO {
    NONE,
    RECEIVED,
    DELIVERED,
    RETURN,
    CLOSED
}
