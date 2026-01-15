package com.primora.erp.contratos.api.dto;

public record ContractDocumentRequest(
        String fileName,
        String contentType
) {
}
