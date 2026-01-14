package com.primora.erp.financeiro.api.dto;

import com.primora.erp.financeiro.domain.FinancialEntryType;
import com.primora.erp.financeiro.domain.FinancialReferenceType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record FinancialEntryResponse(
        UUID id,
        FinancialEntryType type,
        String description,
        BigDecimal amount,
        String currency,
        LocalDate entryDate,
        UUID categoryId,
        UUID departmentId,
        UUID costCenterId,
        FinancialReferenceType referenceType,
        UUID referenceId
) {
}
