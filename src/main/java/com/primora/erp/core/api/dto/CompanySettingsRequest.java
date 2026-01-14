package com.primora.erp.core.api.dto;

import jakarta.validation.constraints.NotBlank;

public record CompanySettingsRequest(
        @NotBlank String timezone,
        @NotBlank String locale,
        @NotBlank String currency
) {
}
