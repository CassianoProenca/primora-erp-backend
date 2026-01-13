package com.primora.erp.iam.api.dto;

import java.util.UUID;

public record PermissionResponse(
        UUID id,
        String code,
        String description
) {
}
