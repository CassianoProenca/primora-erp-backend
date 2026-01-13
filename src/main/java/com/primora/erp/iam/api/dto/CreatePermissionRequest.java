package com.primora.erp.iam.api.dto;

import jakarta.validation.constraints.NotBlank;

public record CreatePermissionRequest(
        @NotBlank String code,
        String description
) {
}
