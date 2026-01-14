package com.primora.erp.rh.api.dto;

import com.primora.erp.rh.domain.ContractStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record EmploymentContractRequest(
        @NotNull UUID employeeId,
        @NotBlank String title,
        @NotNull LocalDate startDate,
        LocalDate endDate,
        @NotNull @DecimalMin("0.00") BigDecimal monthlySalary,
        @NotNull ContractStatus status
) {
}
