package com.primora.erp.rh.api.dto;

import com.primora.erp.rh.domain.EmployeeStatus;
import java.util.UUID;

public record EmployeeResponse(
        UUID id,
        String name,
        String email,
        String document,
        UUID departmentId,
        EmployeeStatus status
) {
}
