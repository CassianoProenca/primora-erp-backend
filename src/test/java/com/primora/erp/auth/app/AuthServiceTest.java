package com.primora.erp.auth.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
class AuthServiceTest {

    @Mock
    private UserJpaRepository userRepository;
    @Mock
    private UserCompanyJpaRepository userCompanyRepository;
    @Mock
    private RefreshTokenJpaRepository refreshTokenRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuditService auditService;

    @InjectMocks
    private AuthService authService;

    @Test
    void login_ShouldReturnTokens_WhenCredentialsAreValid() {
        String email = "test@example.com";
        String password = "password";
        User user = new User(UUID.randomUUID(), email, "hash", "Test User", UserType.TENANT_USER, UserStatus.ACTIVE, Instant.now(), Instant.now(), null);
        UUID companyId = UUID.randomUUID();
        UserCompany userCompany = new UserCompany(UUID.randomUUID(), user.getId(), companyId, "ACTIVE", Instant.now());

        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, user.getPasswordHash())).thenReturn(true);
        when(userCompanyRepository.findByUserId(user.getId())).thenReturn(Optional.of(userCompany));
        when(jwtService.issueAccessToken(eq(user.getId()), eq(user.getUserType()), eq(companyId))).thenReturn("access-token");
        when(jwtService.getRefreshTokenTtlDays()).thenReturn(7L);

        AuthTokens tokens = authService.login(email, password, "127.0.0.1", "agent");

        assertThat(tokens.accessToken()).isEqualTo("access-token");
        assertThat(tokens.refreshToken()).isNotBlank();
        assertThat(tokens.userId()).isEqualTo(user.getId());
        assertThat(tokens.companyId()).isEqualTo(companyId);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
        verify(auditService).log(eq("LOGIN_SUCCESS"), eq(user.getId()), eq(companyId), anyString());
    }

    @Test
    void login_ShouldThrowUnauthorized_WhenUserNotFound() {
        when(userRepository.findByEmailIgnoreCase("wrong@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login("wrong@example.com", "password", "ip", "ua"))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void login_ShouldThrowUnauthorized_WhenPasswordInvalid() {
        String email = "test@example.com";
        User user = new User(UUID.randomUUID(), email, "hash", "Test User", UserType.TENANT_USER, UserStatus.ACTIVE, Instant.now(), Instant.now(), null);

        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", user.getPasswordHash())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(email, "wrong", "ip", "ua"))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
