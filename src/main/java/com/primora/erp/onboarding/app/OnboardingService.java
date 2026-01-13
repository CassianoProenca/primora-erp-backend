package com.primora.erp.onboarding.app;

import com.primora.erp.auth.domain.UserCompany;
import com.primora.erp.auth.domain.UserType;
import com.primora.erp.auth.infra.UserCompanyJpaRepository;
import com.primora.erp.onboarding.domain.Company;
import com.primora.erp.onboarding.domain.OnboardingStatus;
import com.primora.erp.onboarding.infra.CompanyJpaRepository;
import com.primora.erp.shared.security.JwtUser;
import com.primora.erp.shared.security.TenantContext;
import java.time.Instant;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class OnboardingService {

    private final CompanyJpaRepository companyRepository;
    private final UserCompanyJpaRepository userCompanyRepository;

    public OnboardingService(CompanyJpaRepository companyRepository, UserCompanyJpaRepository userCompanyRepository) {
        this.companyRepository = companyRepository;
        this.userCompanyRepository = userCompanyRepository;
    }

    @Transactional
    public Company createCompany(JwtUser user, String legalName, String tradeName, String document) {
        if (user.userType() != UserType.TENANT_USER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only tenant users can create companies");
        }

        if (userCompanyRepository.findByUserId(user.userId()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already linked to a company");
        }

        Instant now = Instant.now();
        Company company = new Company(
                UUID.randomUUID(),
                legalName,
                tradeName,
                document,
                "ACTIVE",
                OnboardingStatus.IN_PROGRESS,
                1,
                user.userId(),
                now,
                now
        );

        companyRepository.save(company);
        userCompanyRepository.save(new UserCompany(
                UUID.randomUUID(),
                user.userId(),
                company.getId(),
                "ACTIVE",
                now
        ));

        return company;
    }

    @Transactional
    public void completeOnboarding() {
        UUID companyId = TenantContext.getCompanyId();
        if (companyId == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Company context missing");
        }

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found"));
        company.markOnboardingCompleted(Instant.now());
        companyRepository.save(company);
    }
}
