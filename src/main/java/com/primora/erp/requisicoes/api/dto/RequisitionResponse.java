package com.primora.erp.requisicoes.api.dto;

import com.primora.erp.requisicoes.domain.RequisitionStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record RequisitionResponse(
        UUID id,
        String title,
        String description,
        UUID requesterDepartmentId,
        UUID targetDepartmentId,
        UUID authorUserId,
        UUID recipientUserId,
        RequisitionStatus status,
        List<RequisitionItemResponse> items,
        Instant createdAt,
        Instant updatedAt
) {
}
