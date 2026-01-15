package com.primora.erp.shared.files;

public record StoredDocument(
        String fileName,
        String contentType,
        String storagePath
) {
}
