package com.primora.erp.rh.api.dto;

import com.primora.erp.rh.domain.EmployeeStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record EmployeeRequest(
        @NotBlank String name,
        String email,
        String document,
        UUID departmentId,
        @NotNull EmployeeStatus status
) {
}
