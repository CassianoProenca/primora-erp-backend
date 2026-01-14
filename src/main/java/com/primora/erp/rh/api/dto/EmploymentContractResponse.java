package com.primora.erp.rh.api.dto;

import com.primora.erp.rh.domain.ContractStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record EmploymentContractResponse(
        UUID id,
        UUID employeeId,
        String title,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal monthlySalary,
        ContractStatus status
) {
}
