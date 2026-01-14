package com.primora.erp.estoque.api.dto;

import com.primora.erp.estoque.domain.StockMovementType;
import com.primora.erp.estoque.domain.StockReferenceType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record StockMovementResponse(
        UUID id,
        UUID warehouseId,
        UUID itemId,
        StockMovementType type,
        BigDecimal quantity,
        BigDecimal unitCost,
        StockReferenceType referenceType,
        UUID referenceId,
        UUID createdByUserId,
        Instant createdAt
) {
}
