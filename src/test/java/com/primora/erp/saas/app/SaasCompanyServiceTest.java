package com.primora.erp.saas.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.primora.erp.onboarding.domain.Company;
import com.primora.erp.onboarding.infra.CompanyJpaRepository;
import com.primora.erp.saas.domain.Subscription;
import com.primora.erp.saas.domain.SubscriptionStatus;
import com.primora.erp.saas.infra.SubscriptionJpaRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class SaasCompanyServiceTest {

    @Mock
    private CompanyJpaRepository companyRepository;
    @Mock
    private SubscriptionJpaRepository subscriptionRepository;

    @InjectMocks
    private SaasCompanyService saasCompanyService;

    @Test
    void getSubscription_ShouldReturnSubscription() {
        UUID companyId = UUID.randomUUID();
        Subscription subscription = new Subscription(
                UUID.randomUUID(), companyId, "BASIC", SubscriptionStatus.ACTIVE,
                Instant.now(), Instant.now().plusSeconds(3600), true, false,
                "STRIPE", "cust_123", "sub_123", Instant.now(), Instant.now()
        );

        when(subscriptionRepository.findByCompanyId(companyId)).thenReturn(Optional.of(subscription));

        Subscription result = saasCompanyService.getSubscription(companyId);

        assertThat(result).isNotNull();
        assertThat(result.getCompanyId()).isEqualTo(companyId);
    }

    @Test
    void getSubscription_WhenNotFound_ShouldThrowNotFound() {
        UUID companyId = UUID.randomUUID();
        when(subscriptionRepository.findByCompanyId(companyId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> saasCompanyService.getSubscription(companyId))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }
    
    @Test
    void getCompany_ShouldReturnCompany() {
        UUID companyId = UUID.randomUUID();
        Company company = new Company(companyId, "L", "T", "D", "ACTIVE", null, null, null, null, null);
        
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        
        Company result = saasCompanyService.getCompany(companyId);
        
        assertThat(result.getId()).isEqualTo(companyId);
    }
}
