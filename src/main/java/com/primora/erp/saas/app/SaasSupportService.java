package com.primora.erp.saas.app;

import com.primora.erp.auth.app.PasswordResetService;
import com.primora.erp.auth.domain.User;
import com.primora.erp.auth.infra.UserJpaRepository;
import com.primora.erp.onboarding.app.OnboardingTokenService;
import com.primora.erp.onboarding.domain.Company;
import com.primora.erp.onboarding.domain.OnboardingStatus;
import com.primora.erp.onboarding.domain.OnboardingTokenCreatedBy;
import com.primora.erp.onboarding.infra.CompanyJpaRepository;
import com.primora.erp.shared.audit.AuditService;
import com.primora.erp.shared.outbox.EmailOutboxService;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SaasSupportService {

    private static final String ONBOARDING_RESENT = "SAAS_ONBOARDING_RESENT";

    private final CompanyJpaRepository companyRepository;
    private final OnboardingTokenService onboardingTokenService;
    private final EmailOutboxService emailOutboxService;
    private final PasswordResetService passwordResetService;
    private final UserJpaRepository userRepository;
    private final AuditService auditService;

    public SaasSupportService(CompanyJpaRepository companyRepository, OnboardingTokenService onboardingTokenService,
                              EmailOutboxService emailOutboxService, PasswordResetService passwordResetService,
                              UserJpaRepository userRepository, AuditService auditService) {
        this.companyRepository = companyRepository;
        this.onboardingTokenService = onboardingTokenService;
        this.emailOutboxService = emailOutboxService;
        this.passwordResetService = passwordResetService;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    @Transactional
    public void resendOnboarding(UUID companyId, UUID actorUserId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found"));
        if (company.getOnboardingStatus() == OnboardingStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Onboarding already completed");
        }
        if (company.getPrimaryAdminUserId() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Company has no primary admin");
        }
        User adminUser = userRepository.findById(company.getPrimaryAdminUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin user not found"));

        String token = onboardingTokenService.issueToken(
                adminUser.getId(),
                company.getId(),
                OnboardingTokenCreatedBy.SUPPORT
        );

        emailOutboxService.queueOnboardingEmail(company.getId(), adminUser.getEmail(), token, actorUserId);
        auditService.log(ONBOARDING_RESENT, actorUserId, company.getId(),
                "{\"companyId\":\"" + company.getId() + "\"}");
    }

    @Transactional
    public void requestAdminPasswordReset(UUID companyId, UUID actorUserId, String ip, String userAgent) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found"));
        UUID adminUserId = company.getPrimaryAdminUserId();
        if (adminUserId == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Company has no primary admin");
        }

        passwordResetService.requestResetBySupport(adminUserId, actorUserId, ip, userAgent);
    }
}
