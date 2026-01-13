package com.primora.erp.shared.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import org.springframework.web.filter.OncePerRequestFilter;

public class TenantContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            Optional<JwtUser> jwtUser = CurrentUser.get();
            if (jwtUser.isPresent() && CurrentUser.isTenantUser(jwtUser.get())) {
                if (jwtUser.get().companyId() == null) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
                TenantContext.setCompanyId(jwtUser.get().companyId());
            }

            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
