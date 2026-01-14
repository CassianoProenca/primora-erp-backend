package com.primora.erp.core.api.dto;

import jakarta.validation.constraints.NotBlank;

public record DepartmentRequest(
        @NotBlank String code,
        @NotBlank String name
) {
}
