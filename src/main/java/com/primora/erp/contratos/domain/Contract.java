package com.primora.erp.contratos.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "contracts")
public class Contract {

    @Id
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column(name = "vendor_name", nullable = false)
    private String vendorName;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContractStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Contract() {
    }

    public Contract(UUID id, UUID companyId, String title, String description, String vendorName,
                    LocalDate startDate, LocalDate endDate, ContractStatus status,
                    Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.companyId = companyId;
        this.title = title;
        this.description = description;
        this.vendorName = vendorName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getCompanyId() {
        return companyId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getVendorName() {
        return vendorName;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public ContractStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void update(String title, String description, String vendorName,
                       LocalDate startDate, LocalDate endDate,
                       ContractStatus status, Instant now) {
        this.title = title;
        this.description = description;
        this.vendorName = vendorName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.updatedAt = now;
    }
}
