package com.primora.erp.estoque.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record StockItemRequest(
        @NotBlank String sku,
        @NotBlank String name,
        @NotBlank String unit,
        @NotNull @DecimalMin("0.00") BigDecimal purchaseUnitCost,
        @NotNull Boolean active
) {
}
