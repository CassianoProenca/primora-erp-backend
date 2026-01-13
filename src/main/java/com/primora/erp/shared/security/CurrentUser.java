package com.primora.erp.shared.security;

import com.primora.erp.auth.domain.UserType;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class CurrentUser {

    private CurrentUser() {
    }

    public static Optional<JwtUser> get() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof JwtUser jwtUser)) {
            return Optional.empty();
        }
        return Optional.of(jwtUser);
    }

    public static boolean isTenantUser(JwtUser jwtUser) {
        return jwtUser.userType() == UserType.TENANT_USER;
    }

    public static boolean isSaasUser(JwtUser jwtUser) {
        return jwtUser.userType() == UserType.SAAS_OWNER || jwtUser.userType() == UserType.SAAS_SUPPORT;
    }
}
