package com.primora.erp.requisicoes.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record RequisitionRequest(
        @NotBlank String title,
        @NotBlank String description,
        @NotNull UUID requesterDepartmentId,
        @NotNull UUID targetDepartmentId,
        UUID recipientUserId,
        @NotEmpty @Valid List<RequisitionItemRequest> items
) {
}
