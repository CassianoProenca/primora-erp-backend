package com.primora.erp.auth.app;

import com.primora.erp.auth.domain.PasswordResetToken;
import com.primora.erp.auth.domain.TokenCreatedBy;
import com.primora.erp.auth.domain.User;
import com.primora.erp.auth.domain.UserStatus;
import com.primora.erp.auth.domain.UserType;
import com.primora.erp.auth.infra.PasswordResetProperties;
import com.primora.erp.auth.infra.PasswordResetTokenJpaRepository;
import com.primora.erp.auth.infra.UserCompanyJpaRepository;
import com.primora.erp.auth.infra.UserJpaRepository;
import com.primora.erp.shared.audit.AuditService;
import com.primora.erp.shared.outbox.EmailOutboxService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PasswordResetService {

    private static final String RESET_REQUESTED = "PASSWORD_RESET_REQUESTED";
    private static final String RESET_COMPLETED = "PASSWORD_RESET_COMPLETED";
    private static final String RESET_SUPPORT_REQUESTED = "PASSWORD_RESET_SUPPORT_REQUESTED";

    private final UserJpaRepository userRepository;
    private final UserCompanyJpaRepository userCompanyRepository;
    private final PasswordResetTokenJpaRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetProperties properties;
    private final EmailOutboxService emailOutboxService;
    private final AuditService auditService;

    public PasswordResetService(UserJpaRepository userRepository, UserCompanyJpaRepository userCompanyRepository,
                                PasswordResetTokenJpaRepository tokenRepository, PasswordEncoder passwordEncoder,
                                PasswordResetProperties properties, EmailOutboxService emailOutboxService,
                                AuditService auditService) {
        this.userRepository = userRepository;
        this.userCompanyRepository = userCompanyRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.properties = properties;
        this.emailOutboxService = emailOutboxService;
        this.auditService = auditService;
    }

    @Transactional
    public void requestReset(String email, String ip, String userAgent) {
        User user = userRepository.findByEmailIgnoreCase(email).orElse(null);
        if (user == null || user.getStatus() != UserStatus.ACTIVE) {
            return;
        }

        String rawToken = UUID.randomUUID().toString();
        String tokenHash = TokenHasher.sha256(rawToken);
        Instant now = Instant.now();
        Instant expiresAt = now.plus(properties.getTokenTtlMinutes(), ChronoUnit.MINUTES);
        PasswordResetToken token = new PasswordResetToken(
                UUID.randomUUID(),
                user,
                tokenHash,
                expiresAt,
                null,
                now,
                TokenCreatedBy.SELF_SERVICE,
                ip,
                userAgent
        );
        tokenRepository.save(token);

        UUID companyId = resolveCompanyId(user);
        emailOutboxService.queuePasswordResetEmail(companyId, user.getEmail(), rawToken, null);
        auditService.log(RESET_REQUESTED, user.getId(), companyId, "{}");
    }

    @Transactional
    public void requestResetBySupport(UUID userId, UUID actorUserId, String ip, String userAgent) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User is not active");
        }

        String rawToken = UUID.randomUUID().toString();
        String tokenHash = TokenHasher.sha256(rawToken);
        Instant now = Instant.now();
        Instant expiresAt = now.plus(properties.getTokenTtlMinutes(), ChronoUnit.MINUTES);
        PasswordResetToken token = new PasswordResetToken(
                UUID.randomUUID(),
                user,
                tokenHash,
                expiresAt,
                null,
                now,
                TokenCreatedBy.SUPPORT,
                ip,
                userAgent
        );
        tokenRepository.save(token);

        UUID companyId = resolveCompanyId(user);
        emailOutboxService.queuePasswordResetEmail(companyId, user.getEmail(), rawToken, actorUserId);
        auditService.log(RESET_SUPPORT_REQUESTED, actorUserId, companyId,
                "{\"targetUserId\":\"" + user.getId() + "\"}");
    }

    @Transactional
    public void confirmReset(String tokenValue, String newPassword) {
        String tokenHash = TokenHasher.sha256(tokenValue);
        PasswordResetToken token = tokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid token"));

        Instant now = Instant.now();
        if (token.isUsed() || token.isExpired(now)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid token");
        }

        User user = token.getUser();
        user.changePasswordHash(passwordEncoder.encode(newPassword), now);
        userRepository.save(user);
        token.markUsed(now);
        tokenRepository.save(token);

        UUID companyId = resolveCompanyId(user);
        auditService.log(RESET_COMPLETED, user.getId(), companyId, "{}");
    }

    private UUID resolveCompanyId(User user) {
        if (user.getUserType() != UserType.TENANT_USER) {
            return null;
        }

        return userCompanyRepository.findByUserId(user.getId())
                .map(userCompany -> userCompany.getCompanyId())
                .orElse(null);
    }
}
