package com.primora.erp.financeiro.api.dto;

import java.util.UUID;

public record FinancialCategoryResponse(
        UUID id,
        String code,
        String name,
        boolean system,
        boolean active
) {
}
