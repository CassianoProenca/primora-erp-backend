package com.primora.erp.auth.api.dto;

import com.primora.erp.auth.domain.UserType;
import java.util.UUID;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        UUID userId,
        UserType userType,
        UUID companyId
) {
}
