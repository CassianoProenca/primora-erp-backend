package com.primora.erp.rh.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record BenefitRequest(
        @NotNull UUID employeeId,
        @NotBlank String name,
        @NotNull @DecimalMin("0.00") BigDecimal amount,
        @NotNull Boolean active
) {
}
