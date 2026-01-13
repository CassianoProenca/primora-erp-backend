package com.primora.erp.iam.api.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Set;

public record CreateRoleRequest(
        @NotBlank String code,
        @NotBlank String name,
        Set<String> permissionCodes
) {
}
