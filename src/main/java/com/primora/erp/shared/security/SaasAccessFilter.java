package com.primora.erp.shared.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import org.springframework.web.filter.OncePerRequestFilter;

public class SaasAccessFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        if (path == null || (!path.equals("/saas") && !path.startsWith("/saas/"))) {
            filterChain.doFilter(request, response);
            return;
        }

        Optional<JwtUser> jwtUser = CurrentUser.get();
        if (jwtUser.isPresent() && CurrentUser.isSaasUser(jwtUser.get())) {
            filterChain.doFilter(request, response);
            return;
        }

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    }
}
