package com.primora.erp.shared.files;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class DocumentStorageService {

    private final FileStorageProperties properties;

    public DocumentStorageService(FileStorageProperties properties) {
        this.properties = properties;
    }

    public StoredDocument storeContractDocument(UUID contractId, MultipartFile file,
                                                String preferredFileName, String preferredContentType) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Document file is required");
        }

        String fileName = resolveFileName(file, preferredFileName);
        String contentType = resolveContentType(file, preferredContentType);
        String uniqueFileName = UUID.randomUUID() + "-" + fileName;

        Path baseDir = Paths.get(properties.getDocumentsDir()).normalize();
        Path contractDir = baseDir.resolve(contractId.toString()).normalize();
        ensureDirectory(contractDir);

        Path target = contractDir.resolve(uniqueFileName).normalize();
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store document");
        }

        String storagePath = baseDir.resolve(contractId.toString()).resolve(uniqueFileName).toString();
        return new StoredDocument(fileName, contentType, storagePath);
    }

    private void ensureDirectory(Path directory) {
        try {
            Files.createDirectories(directory);
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to prepare storage");
        }
    }

    private String resolveFileName(MultipartFile file, String preferredFileName) {
        String rawName = preferredFileName;
        if (rawName == null || rawName.isBlank()) {
            rawName = file.getOriginalFilename();
        }
        if (rawName == null || rawName.isBlank()) {
            rawName = "document";
        }
        return rawName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private String resolveContentType(MultipartFile file, String preferredContentType) {
        if (preferredContentType != null && !preferredContentType.isBlank()) {
            return preferredContentType;
        }
        String contentType = file.getContentType();
        return contentType == null ? "application/octet-stream" : contentType;
    }
}
