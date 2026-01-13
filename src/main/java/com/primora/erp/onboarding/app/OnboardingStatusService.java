package com.primora.erp.onboarding.app;

import com.primora.erp.onboarding.domain.Company;
import com.primora.erp.onboarding.domain.OnboardingStatus;
import com.primora.erp.onboarding.infra.CompanyJpaRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OnboardingStatusService {

    private final CompanyJpaRepository companyRepository;

    public OnboardingStatusService(CompanyJpaRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Transactional(readOnly = true)
    public boolean isOnboardingCompleted(UUID companyId) {
        return companyRepository.findById(companyId)
                .map(Company::getOnboardingStatus)
                .map(status -> status == OnboardingStatus.COMPLETED)
                .orElse(false);
    }
}
