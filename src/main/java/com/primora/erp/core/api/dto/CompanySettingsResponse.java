package com.primora.erp.core.api.dto;

import java.util.UUID;

public record CompanySettingsResponse(
        UUID id,
        String timezone,
        String locale,
        String currency
) {
}
