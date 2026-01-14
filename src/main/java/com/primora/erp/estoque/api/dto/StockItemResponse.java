package com.primora.erp.estoque.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record StockItemResponse(
        UUID id,
        String sku,
        String name,
        String unit,
        BigDecimal purchaseUnitCost,
        boolean active
) {
}
