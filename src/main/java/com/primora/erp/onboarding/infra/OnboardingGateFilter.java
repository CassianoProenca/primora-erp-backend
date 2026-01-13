package com.primora.erp.onboarding.infra;

import com.primora.erp.auth.domain.UserType;
import com.primora.erp.onboarding.app.OnboardingStatusService;
import com.primora.erp.shared.security.CurrentUser;
import com.primora.erp.shared.security.JwtUser;
import com.primora.erp.shared.security.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import org.springframework.web.filter.OncePerRequestFilter;

public class OnboardingGateFilter extends OncePerRequestFilter {

    private final OnboardingStatusService statusService;

    public OnboardingGateFilter(OnboardingStatusService statusService) {
        this.statusService = statusService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        if (path != null && (path.startsWith("/onboarding/") || path.startsWith("/auth/")
                || path.startsWith("/actuator/health"))) {
            filterChain.doFilter(request, response);
            return;
        }

        Optional<JwtUser> jwtUser = CurrentUser.get();
        if (jwtUser.isPresent() && jwtUser.get().userType() == UserType.TENANT_USER) {
            if (TenantContext.getCompanyId() == null) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            boolean completed = statusService.isOnboardingCompleted(TenantContext.getCompanyId());
            if (!completed) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
