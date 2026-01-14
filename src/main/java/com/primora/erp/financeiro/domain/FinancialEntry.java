package com.primora.erp.financeiro.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "financial_entries")
public class FinancialEntry {

    @Id
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FinancialEntryType type;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String currency;

    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    @Column(name = "category_id")
    private UUID categoryId;

    @Column(name = "department_id")
    private UUID departmentId;

    @Column(name = "cost_center_id")
    private UUID costCenterId;

    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type", nullable = false)
    private FinancialReferenceType referenceType;

    @Column(name = "reference_id")
    private UUID referenceId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected FinancialEntry() {
    }

    public FinancialEntry(UUID id, UUID companyId, FinancialEntryType type, String description, BigDecimal amount,
                          String currency, LocalDate entryDate, UUID categoryId, UUID departmentId,
                          UUID costCenterId, FinancialReferenceType referenceType, UUID referenceId,
                          Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.companyId = companyId;
        this.type = type;
        this.description = description;
        this.amount = amount;
        this.currency = currency;
        this.entryDate = entryDate;
        this.categoryId = categoryId;
        this.departmentId = departmentId;
        this.costCenterId = costCenterId;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getCompanyId() {
        return companyId;
    }

    public FinancialEntryType getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public LocalDate getEntryDate() {
        return entryDate;
    }

    public UUID getCategoryId() {
        return categoryId;
    }

    public UUID getDepartmentId() {
        return departmentId;
    }

    public UUID getCostCenterId() {
        return costCenterId;
    }

    public FinancialReferenceType getReferenceType() {
        return referenceType;
    }

    public UUID getReferenceId() {
        return referenceId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
