package com.primora.erp.requisicoes.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ComunicadoRequest(
        @NotBlank String title,
        @NotBlank String message,
        @NotNull UUID requesterDepartmentId,
        @NotNull UUID targetDepartmentId,
        UUID recipientUserId
) {
}
