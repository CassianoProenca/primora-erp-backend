package com.primora.erp.onboarding.api.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateCompanyRequest(
        @NotBlank String legalName,
        @NotBlank String tradeName,
        String document
) {
}
