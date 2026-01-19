package com.primora.erp.onboarding.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.primora.erp.auth.domain.UserCompany;
import com.primora.erp.auth.domain.UserType;
import com.primora.erp.auth.infra.UserCompanyJpaRepository;
import com.primora.erp.onboarding.domain.Company;
import com.primora.erp.onboarding.domain.OnboardingStatus;
import com.primora.erp.onboarding.infra.CompanyJpaRepository;
import com.primora.erp.shared.security.JwtUser;
import com.primora.erp.shared.security.TenantContext;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class OnboardingServiceTest {

    @Mock
    private CompanyJpaRepository companyRepository;
    @Mock
    private UserCompanyJpaRepository userCompanyRepository;

    @InjectMocks
    private OnboardingService onboardingService;

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void createCompany_ShouldSaveCompanyAndLinkUser() {
        UUID userId = UUID.randomUUID();
        JwtUser user = new JwtUser(userId, UserType.TENANT_USER, null);
        
        when(userCompanyRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(companyRepository.save(any(Company.class))).thenAnswer(i -> i.getArgument(0));

        Company company = onboardingService.createCompany(user, "Legal Name", "Trade Name", "123");

        assertThat(company).isNotNull();
        assertThat(company.getLegalName()).isEqualTo("Legal Name");
        assertThat(company.getPrimaryAdminUserId()).isEqualTo(userId);
        assertThat(company.getOnboardingStatus()).isEqualTo(OnboardingStatus.IN_PROGRESS);
        
        verify(userCompanyRepository).save(any(UserCompany.class));
    }
    
    @Test
    void createCompany_WhenUserAlreadyHasCompany_ShouldThrowConflict() {
        UUID userId = UUID.randomUUID();
        JwtUser user = new JwtUser(userId, UserType.TENANT_USER, null);
        
        when(userCompanyRepository.findByUserId(userId)).thenReturn(Optional.of(new UserCompany(UUID.randomUUID(), userId, UUID.randomUUID(), "ACTIVE", null)));

        assertThatThrownBy(() -> onboardingService.createCompany(user, "L", "T", "D"))
            .isInstanceOf(ResponseStatusException.class)
            .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
            .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void completeOnboarding_ShouldUpdateStatus() {
        UUID companyId = UUID.randomUUID();
        TenantContext.setCompanyId(companyId);
        Company company = new Company(companyId, "L", "T", "D", "ACTIVE", OnboardingStatus.IN_PROGRESS, 1, UUID.randomUUID(), null, null);
        
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        
        onboardingService.completeOnboarding();
        
        assertThat(company.getOnboardingStatus()).isEqualTo(OnboardingStatus.COMPLETED);
        verify(companyRepository).save(company);
    }
}
