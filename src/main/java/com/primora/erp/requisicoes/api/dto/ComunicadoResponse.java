package com.primora.erp.requisicoes.api.dto;

import com.primora.erp.requisicoes.domain.ComunicadoStatus;
import java.time.Instant;
import java.util.UUID;

public record ComunicadoResponse(
        UUID id,
        String title,
        String message,
        UUID requesterDepartmentId,
        UUID targetDepartmentId,
        UUID authorUserId,
        UUID recipientUserId,
        ComunicadoStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
