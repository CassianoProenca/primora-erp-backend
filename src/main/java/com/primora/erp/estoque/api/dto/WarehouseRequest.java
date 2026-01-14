package com.primora.erp.estoque.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record WarehouseRequest(
        @NotBlank String code,
        @NotBlank String name,
        @NotNull Boolean active
) {
}
