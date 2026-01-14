package com.primora.erp.estoque.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "stock_levels")
public class StockLevel {

    @Id
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "warehouse_id", nullable = false)
    private UUID warehouseId;

    @Column(name = "item_id", nullable = false)
    private UUID itemId;

    @Column(nullable = false)
    private BigDecimal quantity;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected StockLevel() {
    }

    public StockLevel(UUID id, UUID companyId, UUID warehouseId, UUID itemId, BigDecimal quantity, Instant updatedAt) {
        this.id = id;
        this.companyId = companyId;
        this.warehouseId = warehouseId;
        this.itemId = itemId;
        this.quantity = quantity;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getCompanyId() {
        return companyId;
    }

    public UUID getWarehouseId() {
        return warehouseId;
    }

    public UUID getItemId() {
        return itemId;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void increase(BigDecimal delta, Instant now) {
        this.quantity = this.quantity.add(delta);
        this.updatedAt = now;
    }

    public void decrease(BigDecimal delta, Instant now) {
        this.quantity = this.quantity.subtract(delta);
        this.updatedAt = now;
    }
}
