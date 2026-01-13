package com.primora.erp.onboarding.app;

import com.primora.erp.onboarding.domain.OnboardingToken;
import com.primora.erp.onboarding.domain.OnboardingTokenCreatedBy;
import com.primora.erp.onboarding.infra.OnboardingTokenJpaRepository;
import com.primora.erp.onboarding.infra.OnboardingTokenProperties;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OnboardingTokenService {

    private final OnboardingTokenJpaRepository tokenRepository;
    private final OnboardingTokenProperties properties;

    public OnboardingTokenService(OnboardingTokenJpaRepository tokenRepository, OnboardingTokenProperties properties) {
        this.tokenRepository = tokenRepository;
        this.properties = properties;
    }

    @Transactional
    public String issueToken(UUID userId, UUID companyId, OnboardingTokenCreatedBy createdBy) {
        String rawToken = UUID.randomUUID().toString();
        String tokenHash = sha256(rawToken);
        Instant now = Instant.now();
        Instant expiresAt = now.plus(properties.getTokenTtlHours(), ChronoUnit.HOURS);
        OnboardingToken token = new OnboardingToken(
                UUID.randomUUID(),
                userId,
                companyId,
                tokenHash,
                expiresAt,
                null,
                now,
                createdBy
        );
        tokenRepository.save(token);
        return rawToken;
    }

    private String sha256(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
