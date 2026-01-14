package com.primora.erp.requisicoes.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record RequisitionItemRequest(
        @NotNull UUID itemId,
        @NotNull @DecimalMin("0.001") BigDecimal quantity
) {
}
