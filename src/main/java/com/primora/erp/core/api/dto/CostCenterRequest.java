package com.primora.erp.core.api.dto;

import jakarta.validation.constraints.NotBlank;

public record CostCenterRequest(
        @NotBlank String code,
        @NotBlank String name
) {
}
