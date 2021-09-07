package sigma.software.leovegas.drugstore.store

import sigma.software.leovegas.drugstore.store.api.TransferCertificateRequest
import sigma.software.leovegas.drugstore.store.api.TransferCertificateResponse
import sigma.software.leovegas.drugstore.store.api.TransferStatusDTO

// TransferCertificateRequest -> TransferCertificate Entity

fun TransferCertificateRequest.toTransferCertificate() = TransferCertificate(
    orderId = orderId,
    status = status.toEntity(),
    comment = comment
)

// TransferCertificate entity -> TransferCertificateResponse

fun TransferCertificate.toTransferCertificateResponse() = TransferCertificateResponse(
    id = id ?: -1,
    orderId = orderId,
    status = status.toDTO(),
    comment = comment
)

fun List<TransferCertificate>.toTransferCertificateResponseList() =
    this.map(TransferCertificate::toTransferCertificateResponse)

// TransferStatus <-> TransferStatusDTO

fun TransferStatus.toDTO() = TransferStatusDTO.valueOf(name)

fun TransferStatusDTO.toEntity() = TransferStatus.valueOf(name)
