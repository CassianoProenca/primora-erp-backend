package com.primora.erp.requisicoes.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record RequisitionItemResponse(
        UUID id,
        UUID itemId,
        BigDecimal quantity
) {
}
