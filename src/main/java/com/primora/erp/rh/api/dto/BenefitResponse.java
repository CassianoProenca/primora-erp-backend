package com.primora.erp.rh.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record BenefitResponse(
        UUID id,
        UUID employeeId,
        String name,
        BigDecimal amount,
        boolean active
) {
}
