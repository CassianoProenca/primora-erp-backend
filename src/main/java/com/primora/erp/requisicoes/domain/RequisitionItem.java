package com.primora.erp.requisicoes.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "requisition_items")
public class RequisitionItem {

    @Id
    private UUID id;

    @Column(name = "requisition_id", nullable = false)
    private UUID requisitionId;

    @Column(name = "item_id", nullable = false)
    private UUID itemId;

    @Column(nullable = false)
    private BigDecimal quantity;

    protected RequisitionItem() {
    }

    public RequisitionItem(UUID id, UUID requisitionId, UUID itemId, BigDecimal quantity) {
        this.id = id;
        this.requisitionId = requisitionId;
        this.itemId = itemId;
        this.quantity = quantity;
    }

    public UUID getId() {
        return id;
    }

    public UUID getRequisitionId() {
        return requisitionId;
    }

    public UUID getItemId() {
        return itemId;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }
}
