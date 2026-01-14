package com.primora.erp.estoque.api.dto;

import com.primora.erp.estoque.domain.StockMovementType;
import com.primora.erp.estoque.domain.StockReferenceType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record StockMovementRequest(
        @NotNull UUID warehouseId,
        @NotNull UUID itemId,
        @NotNull @DecimalMin("0.001") BigDecimal quantity,
        @NotNull StockMovementType type,
        @NotNull StockReferenceType referenceType,
        UUID referenceId
) {
}
