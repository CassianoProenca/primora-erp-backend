package com.primora.erp.onboarding.api;

import com.primora.erp.onboarding.api.dto.CompanyResponse;
import com.primora.erp.onboarding.api.dto.CreateCompanyRequest;
import com.primora.erp.onboarding.app.OnboardingService;
import com.primora.erp.onboarding.domain.Company;
import com.primora.erp.shared.audit.AuditService;
import com.primora.erp.shared.security.CurrentUser;
import com.primora.erp.shared.security.JwtUser;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/onboarding")
public class OnboardingController {

    private final OnboardingService onboardingService;
    private final AuditService auditService;

    public OnboardingController(OnboardingService onboardingService, AuditService auditService) {
        this.onboardingService = onboardingService;
        this.auditService = auditService;
    }

    @PostMapping("/companies")
    public ResponseEntity<CompanyResponse> createCompany(@Valid @RequestBody CreateCompanyRequest request) {
        JwtUser user = CurrentUser.get()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated"));
        Company company = onboardingService.createCompany(user, request.legalName(), request.tradeName(),
                request.document());
        auditService.log(
                "ONBOARDING_COMPANY_CREATED",
                user.userId(),
                company.getId(),
                "{\"companyId\":\"" + company.getId() + "\"}"
        );
        return ResponseEntity.ok(toResponse(company));
    }

    @PostMapping("/complete")
    public ResponseEntity<Void> completeOnboarding() {
        JwtUser user = CurrentUser.get()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated"));
        UUID companyId = user.companyId();
        onboardingService.completeOnboarding();
        auditService.log(
                "ONBOARDING_COMPLETED",
                user.userId(),
                companyId,
                "{\"companyId\":\"" + companyId + "\"}"
        );
        return ResponseEntity.noContent().build();
    }

    private CompanyResponse toResponse(Company company) {
        return new CompanyResponse(
                company.getId(),
                company.getLegalName(),
                company.getTradeName(),
                company.getDocument(),
                company.getOnboardingStatus()
        );
    }
}
