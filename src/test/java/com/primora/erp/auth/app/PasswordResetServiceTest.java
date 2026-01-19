package com.primora.erp.auth.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private UserJpaRepository userRepository;
    @Mock
    private UserCompanyJpaRepository userCompanyRepository;
    @Mock
    private PasswordResetTokenJpaRepository tokenRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private PasswordResetProperties properties;
    @Mock
    private EmailOutboxService emailOutboxService;
    @Mock
    private AuditService auditService;

    @InjectMocks
    private PasswordResetService passwordResetService;

    @Test
    void requestReset_WhenUserExistsAndActive_ShouldSaveTokenAndQueueEmail() {
        String email = "test@example.com";
        User user = new User(UUID.randomUUID(), email, "hash", "Test User", UserType.TENANT_USER, UserStatus.ACTIVE, Instant.now(), Instant.now(), null);
        
        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(user));
        when(properties.getTokenTtlMinutes()).thenReturn(30L);

        passwordResetService.requestReset(email, "ip", "ua");

        verify(tokenRepository).save(any(PasswordResetToken.class));
        verify(emailOutboxService).queuePasswordResetEmail(isNull(), eq(email), anyString(), isNull());
    }

    @Test
    void requestReset_WhenUserNotExists_ShouldDoNothing() {
        String email = "unknown@example.com";
        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.empty());

        passwordResetService.requestReset(email, "ip", "ua");

        verify(tokenRepository, never()).save(any());
        verify(emailOutboxService, never()).queuePasswordResetEmail(any(), any(), any(), any());
    }

    @Test
    void confirmReset_WhenTokenValid_ShouldChangePassword() {
        String tokenValue = "valid-token";
        String tokenHash = TokenHasher.sha256(tokenValue);
        User user = new User(UUID.randomUUID(), "test@example.com", "old-hash", "Test User", UserType.TENANT_USER, UserStatus.ACTIVE, Instant.now(), Instant.now(), null);
        PasswordResetToken token = new PasswordResetToken(UUID.randomUUID(), user, tokenHash, Instant.now().plusSeconds(600), null, Instant.now(), TokenCreatedBy.SELF_SERVICE, "ip", "ua");

        when(tokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("new-password")).thenReturn("new-hash");

        passwordResetService.confirmReset(tokenValue, "new-password");

        assertThat(user.getPasswordHash()).isEqualTo("new-hash");
        assertThat(token.isUsed()).isTrue();
        verify(userRepository).save(user);
        verify(tokenRepository).save(token);
    }

    @Test
    void confirmReset_WhenTokenNotFound_ShouldThrowBadRequest() {
        String tokenValue = "invalid-token";
        String tokenHash = TokenHasher.sha256(tokenValue);
        when(tokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> passwordResetService.confirmReset(tokenValue, "new-password"))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
