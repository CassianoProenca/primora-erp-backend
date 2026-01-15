package com.primora.erp.contratos.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "contract_documents")
public class ContractDocument {

    @Id
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "contract_id", nullable = false)
    private UUID contractId;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "storage_url", nullable = false)
    private String storageUrl;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected ContractDocument() {
    }

    public ContractDocument(UUID id, UUID companyId, UUID contractId, String fileName, String contentType,
                            String storageUrl, Instant createdAt) {
        this.id = id;
        this.companyId = companyId;
        this.contractId = contractId;
        this.fileName = fileName;
        this.contentType = contentType;
        this.storageUrl = storageUrl;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getCompanyId() {
        return companyId;
    }

    public UUID getContractId() {
        return contractId;
    }

    public String getFileName() {
        return fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public String getStorageUrl() {
        return storageUrl;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
