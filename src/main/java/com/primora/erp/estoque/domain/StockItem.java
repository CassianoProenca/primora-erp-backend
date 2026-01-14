package com.primora.erp.estoque.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "stock_items")
public class StockItem {

    @Id
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(nullable = false)
    private String sku;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String unit;

    @Column(name = "purchase_unit_cost", nullable = false)
    private BigDecimal purchaseUnitCost;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected StockItem() {
    }

    public StockItem(UUID id, UUID companyId, String sku, String name, String unit,
                     BigDecimal purchaseUnitCost, boolean active, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.companyId = companyId;
        this.sku = sku;
        this.name = name;
        this.unit = unit;
        this.purchaseUnitCost = purchaseUnitCost;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getCompanyId() {
        return companyId;
    }

    public String getSku() {
        return sku;
    }

    public String getName() {
        return name;
    }

    public String getUnit() {
        return unit;
    }

    public BigDecimal getPurchaseUnitCost() {
        return purchaseUnitCost;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void update(String sku, String name, String unit, BigDecimal purchaseUnitCost, boolean active,
                       Instant now) {
        this.sku = sku;
        this.name = name;
        this.unit = unit;
        this.purchaseUnitCost = purchaseUnitCost;
        this.active = active;
        this.updatedAt = now;
    }
}
