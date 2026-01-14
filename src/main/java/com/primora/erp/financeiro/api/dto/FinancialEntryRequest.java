package com.primora.erp.financeiro.api.dto;

import com.primora.erp.financeiro.domain.FinancialEntryType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record FinancialEntryRequest(
        @NotNull FinancialEntryType type,
        @NotBlank String description,
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        String currency,
        @NotNull LocalDate entryDate,
        UUID categoryId,
        UUID departmentId,
        UUID costCenterId
) {
}
