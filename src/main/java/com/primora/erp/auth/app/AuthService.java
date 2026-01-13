package com.primora.erp.auth.app;

import com.primora.erp.auth.domain.RefreshToken;
import com.primora.erp.auth.domain.User;
import com.primora.erp.auth.domain.UserCompany;
import com.primora.erp.auth.domain.UserStatus;
import com.primora.erp.auth.domain.UserType;
import com.primora.erp.auth.infra.RefreshTokenJpaRepository;
import com.primora.erp.auth.infra.UserCompanyJpaRepository;
import com.primora.erp.auth.infra.UserJpaRepository;
import com.primora.erp.shared.audit.AuditService;
import com.primora.erp.shared.security.JwtService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private static final String LOGIN_SUCCESS = "LOGIN_SUCCESS";
    private static final String LOGOUT_SUCCESS = "LOGOUT_SUCCESS";

    private final UserJpaRepository userRepository;
    private final UserCompanyJpaRepository userCompanyRepository;
    private final RefreshTokenJpaRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuditService auditService;

    public AuthService(UserJpaRepository userRepository, UserCompanyJpaRepository userCompanyRepository,
                       RefreshTokenJpaRepository refreshTokenRepository, PasswordEncoder passwordEncoder,
                       JwtService jwtService, AuditService auditService) {
        this.userRepository = userRepository;
        this.userCompanyRepository = userCompanyRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.auditService = auditService;
    }

    @Transactional
    public AuthTokens login(String email, String rawPassword, String ip, String userAgent) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not active");
        }

        UUID companyId = resolveCompanyId(user);
        String accessToken = jwtService.issueAccessToken(user.getId(), user.getUserType(), companyId);
        String refreshTokenValue = UUID.randomUUID().toString();
        RefreshToken refreshToken = buildRefreshToken(user, refreshTokenValue, ip, userAgent);

        Instant now = Instant.now();
        user.markLogin(now);
        userRepository.save(user);
        refreshTokenRepository.save(refreshToken);
        auditService.log(LOGIN_SUCCESS, user.getId(), companyId, "{}");

        return new AuthTokens(accessToken, refreshTokenValue, user.getId(), user.getUserType(), companyId);
    }

    @Transactional
    public AuthTokens refresh(String refreshTokenValue, String ip, String userAgent) {
        String tokenHash = TokenHasher.sha256(refreshTokenValue);
        RefreshToken storedToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

        Instant now = Instant.now();
        if (storedToken.isRevoked() || storedToken.isExpired(now)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }

        User user = storedToken.getUser();
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not active");
        }

        UUID companyId = resolveCompanyId(user);
        String accessToken = jwtService.issueAccessToken(user.getId(), user.getUserType(), companyId);
        String newRefreshTokenValue = UUID.randomUUID().toString();
        RefreshToken newToken = buildRefreshToken(user, newRefreshTokenValue, ip, userAgent);

        storedToken.revoke(now, newToken.getId());
        refreshTokenRepository.save(storedToken);
        refreshTokenRepository.save(newToken);

        return new AuthTokens(accessToken, newRefreshTokenValue, user.getId(), user.getUserType(), companyId);
    }

    @Transactional
    public void logout(String refreshTokenValue) {
        String tokenHash = TokenHasher.sha256(refreshTokenValue);
        Optional<RefreshToken> storedToken = refreshTokenRepository.findByTokenHash(tokenHash);
        if (storedToken.isEmpty()) {
            return;
        }

        RefreshToken token = storedToken.get();
        if (!token.isRevoked()) {
            token.revoke(Instant.now(), null);
            refreshTokenRepository.save(token);
        }

        User user = token.getUser();
        UUID companyId = resolveCompanyId(user);
        auditService.log(LOGOUT_SUCCESS, user.getId(), companyId, "{}");
    }

    private UUID resolveCompanyId(User user) {
        if (user.getUserType() != UserType.TENANT_USER) {
            return null;
        }

        UserCompany userCompany = userCompanyRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "User has no company"));
        return userCompany.getCompanyId();
    }

    private RefreshToken buildRefreshToken(User user, String rawToken, String ip, String userAgent) {
        String tokenHash = TokenHasher.sha256(rawToken);
        Instant now = Instant.now();
        Instant expiresAt = now.plus(jwtService.getRefreshTokenTtlDays(), ChronoUnit.DAYS);
        return new RefreshToken(UUID.randomUUID(), user, tokenHash, expiresAt, now, ip, userAgent);
    }
}
