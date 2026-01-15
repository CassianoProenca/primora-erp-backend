package com.primora.erp.contratos.api.dto;

import com.primora.erp.contratos.domain.ContractStatus;
import java.time.LocalDate;
import java.util.UUID;

public record ContractResponse(
        UUID id,
        String title,
        String description,
        String vendorName,
        LocalDate startDate,
        LocalDate endDate,
        ContractStatus status
) {
}
