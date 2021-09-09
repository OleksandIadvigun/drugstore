package sigma.software.leovegas.drugstore.store.api

// Requests

data class TransferCertificateRequest(
    val orderNumber: Long = -1,
    val status: TransferStatusDTO = TransferStatusDTO.NONE,
    val comment: String = "undefined",
)

// Responses

data class TransferCertificateResponse(
    val id: Long = -1,
    val orderNumber: Long = -1,
    val status: TransferStatusDTO = TransferStatusDTO.NONE,
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
