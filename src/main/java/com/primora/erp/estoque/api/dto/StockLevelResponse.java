package com.primora.erp.estoque.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record StockLevelResponse(
        UUID itemId,
        BigDecimal quantity,
        Instant updatedAt
) {
}
