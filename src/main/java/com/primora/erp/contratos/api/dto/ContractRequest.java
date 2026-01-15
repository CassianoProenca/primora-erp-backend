package com.primora.erp.contratos.api.dto;

import com.primora.erp.contratos.domain.ContractStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ContractRequest(
        @NotBlank String title,
        @NotBlank String description,
        @NotBlank String vendorName,
        @NotNull LocalDate startDate,
        LocalDate endDate,
        @NotNull ContractStatus status
) {
}
