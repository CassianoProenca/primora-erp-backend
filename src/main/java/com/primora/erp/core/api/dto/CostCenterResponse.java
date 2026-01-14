package com.primora.erp.core.api.dto;

import com.primora.erp.core.domain.RecordStatus;
import java.util.UUID;

public record CostCenterResponse(
        UUID id,
        String code,
        String name,
        RecordStatus status
) {
}
