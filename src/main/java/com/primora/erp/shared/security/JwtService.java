package com.primora.erp.shared.security;

import com.primora.erp.auth.domain.UserType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final JwtProperties properties;
    private final SecretKey key;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        this.key = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String issueAccessToken(UUID userId, UserType userType, UUID companyId) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(properties.getAccessTokenTtlMinutes(), ChronoUnit.MINUTES);

        return Jwts.builder()
                .issuer(properties.getIssuer())
                .subject(userId.toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .claim("type", userType.name())
                .claim("companyId", companyId == null ? null : companyId.toString())
                .signWith(key)
                .compact();
    }

    public Optional<JwtUser> parseAccessToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            UUID userId = UUID.fromString(claims.getSubject());
            UserType userType = UserType.valueOf(claims.get("type", String.class));
            String companyIdValue = claims.get("companyId", String.class);
            UUID companyId = companyIdValue == null ? null : UUID.fromString(companyIdValue);

            return Optional.of(new JwtUser(userId, userType, companyId));
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    public long getRefreshTokenTtlDays() {
        return properties.getRefreshTokenTtlDays();
    }
}
