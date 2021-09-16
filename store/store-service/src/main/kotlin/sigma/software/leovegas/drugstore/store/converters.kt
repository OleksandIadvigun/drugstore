package sigma.software.leovegas.drugstore.store

import sigma.software.leovegas.drugstore.store.api.TransferCertificateResponse
import sigma.software.leovegas.drugstore.store.api.TransferStatusDTO

// TransferCertificate entity -> TransferCertificateResponse

fun TransferCertificate.toTransferCertificateResponse() = TransferCertificateResponse(
    certificateNumber = certificateNumber,
    orderNumber = orderNumber,
    status = status.toDTO(),
    comment = comment
)

fun List<TransferCertificate>.toTransferCertificateResponseList() =
    this.map(TransferCertificate::toTransferCertificateResponse)

// TransferStatus <-> TransferStatusDTO

fun TransferStatus.toDTO() = TransferStatusDTO.valueOf(name)

fun TransferStatusDTO.toEntity() = TransferStatus.valueOf(name)
