package com.primora.erp.financeiro.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FinancialCategoryRequest(
        @NotBlank String code,
        @NotBlank String name,
        @NotNull Boolean active
) {
}
