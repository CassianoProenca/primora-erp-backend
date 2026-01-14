package com.primora.erp.estoque.api.dto;

import java.util.UUID;

public record WarehouseResponse(
        UUID id,
        String code,
        String name,
        boolean active
) {
}
