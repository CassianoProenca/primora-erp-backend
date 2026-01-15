package com.primora.erp.contratos.api.dto;

import java.time.Instant;
import java.util.UUID;

public record ContractDocumentResponse(
        UUID id,
        UUID contractId,
        String fileName,
        String contentType,
        String storageUrl,
        Instant createdAt
) {
}
