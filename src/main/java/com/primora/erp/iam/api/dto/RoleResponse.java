package com.primora.erp.iam.api.dto;

import java.util.Set;
import java.util.UUID;

public record RoleResponse(
        UUID id,
        String code,
        String name,
        boolean system,
        Set<String> permissionCodes
) {
}
