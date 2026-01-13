package com.primora.erp.shared.security;

import com.primora.erp.auth.domain.UserType;
import java.util.UUID;

public record JwtUser(UUID userId, UserType userType, UUID companyId) {
}
